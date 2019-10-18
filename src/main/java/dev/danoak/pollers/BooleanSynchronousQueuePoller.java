package dev.danoak.pollers;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@Slf4j
public class BooleanSynchronousQueuePoller {

    private final BlockingQueue<Boolean> resultQueue = new SynchronousQueue<>();

    public Boolean poll(Callable<Boolean> pollee,
                        long period, TimeUnit periodTimeUnit,
                        long timeout, TimeUnit timeoutTimeUnit) {
        new Thread(() -> {
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
        }).start();
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
