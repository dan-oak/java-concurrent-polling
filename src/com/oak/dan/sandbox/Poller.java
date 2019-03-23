package com.oak.dan.sandbox;

import com.oak.dan.sandbox.functional.*;
import static java.lang.System.*;
import static java.util.concurrent.TimeUnit.*;
import java.util.*;
import java.util.concurrent.*;

public class Poller<Result> {
    private final BlockingQueue<Result>
        q = new ArrayBlockingQueue<>(1, true);
    private final ScheduledThreadPoolExecutor
        e = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?>
        f;
    public Poller(SafeCallable<Optional<Result>> c, int initialDelay, long period, TimeUnit u) {
        this.f = e.scheduleAtFixedRate(() -> put(c), initialDelay, period, u);
        if (initialDelay != 0) out.format("[%s] Starts %h in %d %s with period of %d %s%n",
            getClass().getName(), c, initialDelay, u, period, u);
        else out.format("[%s] Started %h with period of %d %s%n", getClass().getName(), c, period, u);
    }
    public Poller(SafeCallable<Optional<Result>> c) {
        this(c, 0, 1, SECONDS);
    }
    public Optional<Result> poll(long timeout, TimeUnit u) {
        try {
            final Optional<Result> o = Optional.ofNullable(q.poll(timeout, u));
            return o;
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            return null;
        } finally {
            stop();
        }
    }
    private void put(SafeCallable<Optional<Result>> c) {
        final Optional<Result> o = c.call();
        if (o.isPresent()) {
            final Result v = o.get();
            out.format("[%s] Got %s%n", getClass().getName(), v);
            UnsafeRunnable.safe(() -> q.put(v)).run();
        } else {
            out.format("[%s] Nothing yet%n", getClass().getName());
        }
    }
    private void stop() {
        boolean s = f.cancel(false);
        out.format("[%s] %s%n", getClass().getName(), (s ? "Stopped" : "Failed to stop"));
        e.shutdown();
    }

}