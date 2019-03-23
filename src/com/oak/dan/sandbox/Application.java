package com.oak.dan.sandbox;

import com.oak.dan.sandbox.jobs.*;
import com.oak.dan.sandbox.repositories.*;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.*;

class Application {
    public void run() {
        out.format("[%s] Started%n", getClass().getName());

        // given
        ListRepository<Integer> ints = new ListRepository<>();

        // when
        RandomGenerator rig = new RandomGenerator(ints).start();

        // then
        assertTrue("Int divisible by 4 is present", findMod4(ints).isPresent());

        rig.stop();

        out.format("[%s] Finished%n", getClass().getName());
    }

    private void assertTrue(String assertion, boolean result) {
        if (result) out.format("[%s] %s%n", getClass().getName(), assertion);
        else out.format("[%s] Assertion failed: %s%n", getClass().getName(), assertion);
    }

    private Optional<Integer> findMod4(ListRepository<Integer> ints) {
        return new Poller<>(() -> ints.findFirst(i -> i % 4 == 0))
            .poll(5, SECONDS);
    }
}
