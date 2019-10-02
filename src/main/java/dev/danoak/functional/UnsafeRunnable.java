package dev.danoak.functional;

@FunctionalInterface
public interface UnsafeRunnable {
    void run() throws Exception;
    static Runnable safe(UnsafeRunnable r) { return () -> {
        try { r.run(); } catch (Exception ignored) {}
    }; }
}