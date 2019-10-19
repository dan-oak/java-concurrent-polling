package dev.danoak.pollers;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;

@SuppressWarnings("Duplicates")
@Slf4j
public class ScheduledSynchronousQueuePoller<Answer> {

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
