# Concurrent Polling in Java

Once I wondered: 

_What's the best way to poll in an asynchronous Java application?_

## Intention

This article is intended to provide practical usage examples of `java.concurrent` classes by building different 
asynchronous pollers. Fully working examples and many variations can be found [in the repository][repo].

It might be useful to you if you are learning tools in Java concurrency or just need some simple and efficient
poller implementation. 

I worked on a service responsible for Transfer State Machine in [TransferWise][tw], one of the central services 
which managed the integrity of the Transfer data, was defining all possible State Transitions and maintained strict 
order of events processing. All Transfer actions there went through asynchronous channels, which are an interesting 
topic alone, but for now I'll show you a way to conveniently test asynchronous flows by polling.        

## Tests
  
Polling is best suited for integration tests. We have been launching an application, sending some events, commands or 
messages to the async channels and waiting for a data change in a database. On a toy example, something like:
 
```groovy
def "eventually might generate something divisible by 4"() {
    given:
        def repo = new ListRepository<Integer>()
        def rng = new RandomIntegerPersister(repo)
        rng.start(1, 1, SECONDS)                     // Spawn our worker thread 

    expect:
        pollForIsDivisibleBy(repo, 4)                // Spawn our polling thread
            .map({ log.info("Found: {}", it); it })  // Log some info about found object when needed
            .isPresent()                             // And make the test assertion itself 
}

def <T> Optional<T> pollForIsDivisibleBy(Repository<T> repo, T n) throws InterruptedException {
    // Poll every 1 second with timeout 5 seconds:
    return poller.poll({ repo.findFirst({ it % n == 0 }) }, 1, SECONDS, 5, SECONDS)
}
```

## True or False

Now when you see how the test might look like in the end, here is how we can implement and use the most naive version
of a simple poller which returns only a boolean result:

```groovy
given:
    // ...    
    def pollee = {
        repo.findFirst({ it % 4 == 0 })
            .map({ log.info("Found: {}", it); it })
            .isPresent()
    }
expect:
    poller.poll(pollee, 1, SECONDS, 5, SECONDS)
```

```java
public class BooleanThreadPoller {
    public Boolean poll(Callable<Boolean> pollee,
                        long period, TimeUnit periodTimeUnit,
                        long timeout, TimeUnit timeoutTimeUnit
    ) {
        AtomicReference<Boolean> resultRef = new AtomicReference<>();
        Thread poller = new Thread(() -> {
            try {
                long startMs = System.currentTimeMillis();
                long timeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeoutTimeUnit);
                while (true) {
                    if (System.currentTimeMillis() - startMs > timeoutMs) {
                        return;                                                             // Timed out
                    }
                    if (pollee.call()) {
                        resultRef.set(true);
                        return;                                                             // Success
                    } else {
                        periodTimeUnit.sleep(period);                                       // Waiting
                    }
                }
            } catch (Exception e) { }
        });
        poller.start();
        try { poller.join(); } catch (InterruptedException e) { }                           // Waiting
        return resultRef.get();
    }
}
```

The first thing to notice here is `AtomicReference` which is used as an _effectively `final`_ wrapper 
around the result. Without supplying a final or effectively final variable to lambda Java does not compile.

Also there is a significant difference between `new Thread()` `.start()`/`.run()`. `Thread` implements `Runnable` 
but `run` does not actually start execution in a new thread as one might expect, instead it just executes in 
a current thread, but `start` in a new one.

## Schedulers

Now we can sacrifice a bit of flexibility for the sake of a more common approach, using `ScheduledThreadPoolExecutor`: 

```java
public class Poller<Answer> {

    public Optional<Answer> poll(Callable<Optional<Answer>> question,
                                 long period, TimeUnit periodTimeUnit,
                                 long timeout, TimeUnit timeoutTimeUnit
    ) throws InterruptedException {
        SynchronousQueue<Optional<Answer>> answerQueue = new SynchronousQueue<>();
        Runnable questionRunnable = () -> {
            try {
                answerQueue.put(question.call());
            } catch (Exception e) {
                log.error("Polling error", e);
            }
        };
        ScheduledFuture<?> scheduledFuture = Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(questionRunnable, 0, period, periodTimeUnit);
        log.info("Started with period of {} {}", period, periodTimeUnit);
        Optional<Answer> answer = answerQueue.poll(timeout, timeoutTimeUnit);
        scheduledFuture.cancel(false);
        return answer;
    }

}
```

The only problem is that the scheduled task never stops. To solve it we just use `SynchronousQueue` to execute and 
set results from the scheduler, and wait for them on the main thread. When we got a result or timed out, the task is
stopped.

That's it for now.

Regards,  
Dan

## References

When I was looking for examples, I've found many notes about `BlockingQueue` but no one actually showed 
anything specific, it motivated me to write this, in the process of which I learned more and discovered that
we don't even need any implementations of `BlockingQueue`, although the version with `SynchronousQueue` is 
quite clean and efficient.

Thanks to the authors of the following posts:

- "Implement responsive Java polling: https://blog.codecentric.de/en/2018/09/implement-responsive-java-polling/
- CountDownLatch: https://stackoverflow.com/questions/17827022/how-is-countdownlatch-used-in-java-multithreading
- `new Thread()` `.start()`/`.run()`: https://javarevisited.blogspot.com/2012/03/difference-between-start-and-run-method.html
- `Thread.sleep`/`TimeUnit.sleep`: https://stackoverflow.com/questions/9587673/thread-sleep-vs-timeunit-seconds-sleep
- `synchronized`: https://www.baeldung.com/java-synchronized
- Java Queue implementations: https://docs.oracle.com/javase/tutorial/collections/implementations/queue.html
- `SynchronousQueue`: https://www.youtube.com/watch?v=QCMt324j64U
- Effectively final: https://www.baeldung.com/java-lambda-effectively-final-local-variables
- Stop execution of a ScheduledFuture at some point: https://stackoverflow.com/a/7376085/2601742 

One of the popular polling libraries: [Awaitility][await]. It works well with JUnit and Hamcrest matchers, but at the 
time of writing it lacks functionality just to poll and get the result as a return value.

[repo]: https://github.com/danylo-dubinin/java-concurrent-polling/
[tw]: https://transferwise.com
[await]: https://github.com/awaitility/awaitility/
