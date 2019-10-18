package dev.danoak.pollers;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressWarnings("DuplicatedCode")
@Slf4j
public class BooleanLatchedPoller {

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        1, 1, 3, SECONDS,
        new ArrayBlockingQueue<>(1, true));

    public CountDownLatch poll(Callable<Boolean> pollee, long period, TimeUnit timeUnit) {
        CountDownLatch latch = new CountDownLatch(1);
        threadPoolExecutor.submit(() -> {
            try {
                boolean done = false;
                while (!done) {
                    if (pollee.call()) {
                        latch.countDown();
                        done = true;
                        log.info("Polling finished");
                    } else {
                        timeUnit.sleep(period);
                        done = false;
                        log.info("Polling continues");
                    }
                }
            } catch (InterruptedException e) {
                log.error("Interrupted polling", e);
            } catch (Exception e) {
                log.error("Polling error", e);
            }
        });
        log.info("Started with period of {} {}", period, timeUnit);
        return latch;
    }

}
