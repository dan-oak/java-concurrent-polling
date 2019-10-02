package dev.danoak.jobs;

import dev.danoak.repositories.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RandomIntegerPersister implements Job {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final Repository<Integer> repo;

    private final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    private ScheduledFuture<?> future;

    public RandomIntegerPersister(Repository<Integer> r) {
        this.repo = r;
    }

    public RandomIntegerPersister start(int initialDelay, int period, TimeUnit timeUnit) {
        future = threadPoolExecutor.scheduleAtFixedRate(this::exec, initialDelay, period, timeUnit);
        if (initialDelay != 0) {
            log.info("Starts in {} {} with period of {} {}", initialDelay, timeUnit, period, timeUnit);
        } else {
            log.info("Started with period of {} {}", period, timeUnit);
        }
        return this;
    }

    public RandomIntegerPersister stop() {
        if (future != null) {
            future.cancel(true);
        }
        threadPoolExecutor.shutdown();
        log.info("Stopped");
        return this;
    }

    private void exec() {
        Optional<Integer> n = repo.save(new Random().nextInt(10));
        if (n.isPresent()) {
            log.info("Saved {}", n.get());
        } else {
            log.info("Failed");
        }
    }

    public String toString() {
        return repo.toString();
    }

}
