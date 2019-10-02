package dev.danoak.jobs;

import java.util.concurrent.TimeUnit;

public interface Job {
    Job start(int initialDelay, int period, TimeUnit u);
    Job stop();
}
