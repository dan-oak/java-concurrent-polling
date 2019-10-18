package dev.danoak.pollers;

import dev.danoak.functional.SafeCallable;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;

@SuppressWarnings("DuplicatedCode")
@Slf4j
public class ScheduledArrayBlockingQueuePoller<Result> {

    private final BlockingQueue<Result> resultQueue = new ArrayBlockingQueue<>(1, true);

    private final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    private ScheduledFuture<?> future;

    public Optional<Result> poll(SafeCallable<Optional<Result>> pollee,
                                 int period, TimeUnit periodTimeUnit,
                                 int timeout, TimeUnit timeoutTimeUnit) {
        future = threadPoolExecutor.scheduleAtFixedRate(() -> poll(pollee), 0, period, periodTimeUnit);
        log.info("Started with period of {} {}", period, periodTimeUnit);
        final Optional<Result> resultOpt = Optional.ofNullable(get(timeout, timeoutTimeUnit));
        if (resultOpt.isEmpty()) {
            log.error("Polling timed out");
        }
        return resultOpt;

    }

    private void poll(SafeCallable<Optional<Result>> pollee) {
        final Optional<Result> opt = pollee.call();
        if (opt.isPresent()) {
            final Result result = opt.get();
            log.info("Got {}", result);
            put(result);
        } else {
            log.info("Nothing yet");
        }
    }

    private Result get(int timeout, TimeUnit timeoutTimeUnit) {
        try {
            return resultQueue.poll(timeout, timeoutTimeUnit);
        } catch (InterruptedException e) {
            log.error("Polling error", e);
            return null;
        } finally {
            stop();
        }
    }

    private void put(Result result) {
        try {
            resultQueue.put(result);
        } catch (InterruptedException e) {
            log.error("Polling error", e);
        } finally {
            stop();
        }
    }

    private void stop() {
        if (future != null) {
            future.cancel(true);
        }
        threadPoolExecutor.shutdown();
        log.info("Stopped");
    }

}