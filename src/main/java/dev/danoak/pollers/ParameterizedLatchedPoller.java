package dev.danoak.pollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ParameterizedLatchedPoller<Result> {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        1, 1, 3, SECONDS,
        new ArrayBlockingQueue<>(1, true));

    public Optional<Result> poll(Callable<Optional<Result>> pollee,
                                 int period, TimeUnit periodTimeUnit,
                                 int timeout, TimeUnit timeoutTimeUnit) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Result> resultRef = new AtomicReference<>(null);
        threadPoolExecutor.submit(() -> {
            try {
                boolean done = false;
                while (!done) {
                    final Optional<Result> result = pollee.call();
                    if (result.isPresent()) {
                        latch.countDown();
                        done = true;
                        resultRef.set(result.get());
                        log.info("Polling finished. Got: {}", result);
                    } else {
                        periodTimeUnit.sleep(period);
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
        log.info("Started with period of {} {}", period, periodTimeUnit);
        boolean success = false;
        try {
            success = latch.await(timeout, timeoutTimeUnit);
        } catch (InterruptedException e) {
            log.error("Interrupted polling");
        }
        if (success) {
            return Optional.ofNullable(resultRef.get());
        } else {
            return Optional.empty();
        }
    }

}
