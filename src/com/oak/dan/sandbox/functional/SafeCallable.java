package com.oak.dan.sandbox.functional;

@FunctionalInterface
public interface SafeCallable<V> extends java.util.concurrent.Callable<V> {
    V call();
}