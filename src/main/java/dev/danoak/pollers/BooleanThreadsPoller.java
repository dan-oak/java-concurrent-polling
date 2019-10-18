package dev.danoak.pollers;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
@Slf4j
public class BooleanThreadsPoller {

    public Boolean poll(Callable<Boolean> pollee,
                        long period, TimeUnit periodTimeUnit,
                        long timeout, TimeUnit timeoutTimeUnit) {
        AtomicReference<Boolean> resultRef = new AtomicReference<>();
        Thread setter = new Thread(() -> {
            try {
                boolean done = false;
                while (!done) {
                    if (pollee.call()) {
                        resultRef.set(true);
                        done = true;
                        log.info("Setter polling finished");
                    } else {
                        periodTimeUnit.sleep(period);
                        done = false;
                        log.info("Setter polling continues");
                    }
                }
            } catch (InterruptedException e) {
                log.error("Interrupted setter polling", e);
            } catch (Exception e) {
                log.error("Setter polling error", e);
            }
        });
        setter.start();
        log.info("Started with period of {} {}", period, periodTimeUnit);
        Thread getter = new Thread(() -> {
            try {
                boolean done = false;
                long startMs = System.currentTimeMillis();
                long timeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeoutTimeUnit);
                while (!done) {
                    Boolean result = resultRef.get();
                    if (result != null) {
                        done = true;
                        log.info("Getter polling finished");
                    } else {
                        periodTimeUnit.sleep(period);
                        done = false;
                        log.info("Getter polling continues");
                    }
                    if (System.currentTimeMillis() - startMs > timeoutMs) {
                        throw new TimeoutException("Getter polling timed out");
                    }
                }
            } catch (InterruptedException e) {
                log.error("Interrupted getter polling", e);
            } catch (Exception e) {
                log.error("Getter polling error", e);
            }
        });
        getter.start();
        try {
            getter.join();
        } catch (InterruptedException e) {
            log.error("Interrupted polling", e);
        }
        return resultRef.get();
    }

}
