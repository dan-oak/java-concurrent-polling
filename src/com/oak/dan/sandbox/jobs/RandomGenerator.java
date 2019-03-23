package com.oak.dan.sandbox.jobs;

import java.util.*;
import java.util.concurrent.*;
import com.oak.dan.sandbox.repositories.*;
import static java.lang.System.*;

public class RandomGenerator implements Job {
    private final Repository<Integer>
        r;
    private ScheduledThreadPoolExecutor
        e;
    private ScheduledFuture<?>
        f;
    public RandomGenerator(Repository<Integer> r) {
        this.r = r;
    }
    public RandomGenerator start(int initialDelay, int period, TimeUnit u) {
        if (e == null || e.isShutdown()) e = new ScheduledThreadPoolExecutor(1);
        f = e.scheduleAtFixedRate(this::gen, initialDelay, period, u);
        if (initialDelay != 0) out.format("[%s] Starts in %d %s with period of %d %s%n",
            getClass().getName(), initialDelay, u, period, u);
        else out.format("[%s] Started with period of %d %s%n", getClass().getName(), period, u);
        return this;
    }
    public RandomGenerator start() {
        return start(0, 1, TimeUnit.SECONDS);
    }
    public RandomGenerator stop() {
        boolean s = f.cancel(true);
        out.format("[%s] %s%n", getClass().getName(), s ? "Stopped" : "Failed to stop");
        e.shutdown();
        return this;
    }
    private void gen() {
        Optional<Integer> i = r.save(new Random().nextInt(10));
        if (i.isPresent()) out.format("[%s] Saved %d%n", getClass().getName(), i.get());
        else out.format("[%s] Failed%n", getClass().getName());
    }
}
