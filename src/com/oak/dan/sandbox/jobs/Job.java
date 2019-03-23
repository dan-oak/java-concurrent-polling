package com.oak.dan.sandbox.jobs;

import java.util.concurrent.*;

public interface Job {
    Job start(int initialDelay, int period, TimeUnit u);
    Job stop();
}
