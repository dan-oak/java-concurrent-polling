package dev.danoak.pollers;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
@Slf4j
public class ThreadPoller<Result> {

    public Optional<Result> poll(Callable<Optional<Result>> pollee,
                                 long period, TimeUnit periodTimeUnit,
                                 long timeout, TimeUnit timeoutTimeUnit) {
        AtomicReference<Optional<Result>> resultRef = new AtomicReference<>();
        Thread poller = new Thread(() -> {
            try {
                boolean done = false;
                long startMs = System.currentTimeMillis();
                long timeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeoutTimeUnit);
                while (!done) {
                    if (System.currentTimeMillis() - startMs > timeoutMs) {
                        throw new TimeoutException("Polling timed out");
                    }
                    Optional<Result> result = pollee.call();
                    if (result.isPresent()) {
                        resultRef.set(result);
                        done = true;
                        log.info("Polling finished");
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
        poller.start();
        log.info("Started with period of {} {}", period, periodTimeUnit);
        try {
            poller.join();
        } catch (InterruptedException e) {
            log.error("Interrupted polling", e);
        }
        return resultRef.get();
    }

}
