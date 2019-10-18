package dev.danoak.pollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressWarnings("Duplicates")
public class BooleanPoller {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final BlockingQueue<Boolean> resultQueue = new ArrayBlockingQueue<>(1, true);

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
        1, 1, 3, SECONDS,
        new ArrayBlockingQueue<>(1, true));

    public Boolean poll(Callable<Boolean> pollee,
                        long period, TimeUnit periodTimeUnit,
                        long timeout, TimeUnit timeoutTimeUnit) {
        threadPoolExecutor.submit(() -> {
            try {
                boolean done = false;
                while (!done) {
                    if (pollee.call()) {
                        resultQueue.put(true);
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
        log.info("Started with period of {} {}", period, periodTimeUnit);
        final Boolean result;
        try {
            result = resultQueue.poll(timeout, timeoutTimeUnit);
            if (result == null) {
                log.error("Poller timed out");
            }
            return result;
        } catch (InterruptedException e) {
            log.error("Polling error", e);
            return null;
        }
    }

}
