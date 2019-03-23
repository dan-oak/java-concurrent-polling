package com.oak.dan.sandbox.functional;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface UnsafeCallable<V> {
    V call() throws Exception;
    static <V> SafeCallable<V> safe(Callable<V> c) { return () -> {
        try { return c.call(); } catch (Exception ignored) { return null; }
    }; }
}