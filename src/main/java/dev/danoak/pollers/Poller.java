package dev.danoak.pollers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
@Slf4j
public class Poller<Answer> {

    @RequiredArgsConstructor
    private final class QuestionRunnable implements Runnable {

        ScheduledFuture<?> scheduledFuture = null;
        final Callable<Optional<Answer>> question;
        final AtomicReference<Optional<Answer>> answer;

        @Override
        public synchronized void run() {
            if (answer.get().isPresent()) {
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(false);
                } else {
                    try { wait(); } catch (InterruptedException e) { }
                }
            } else {
                try { answer.set(question.call()); } catch (Exception e) { }
            }
        }

        synchronized void bind(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
            notify();
        }

    }

    public Optional<Answer> poll(Callable<Optional<Answer>> question,
                                 long period, TimeUnit periodTimeUnit,
                                 long timeout, TimeUnit timeoutTimeUnit
    ) throws InterruptedException, ExecutionException, TimeoutException {
        AtomicReference<Optional<Answer>> resultRef = new AtomicReference<>(Optional.empty());
        QuestionRunnable questionRunnable = new QuestionRunnable(question, resultRef);
        ScheduledFuture<?> scheduledFuture = Executors
            .newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(questionRunnable, 0, period, periodTimeUnit);
        questionRunnable.bind(scheduledFuture);
        scheduledFuture.get(timeout, timeoutTimeUnit);
        return resultRef.get();
    }

}
