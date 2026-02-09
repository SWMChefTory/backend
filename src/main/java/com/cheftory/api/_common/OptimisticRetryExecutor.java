package com.cheftory.api._common;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OptimisticRetryExecutor {

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception> {
        T get() throws E;
    }

    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception> {
        void run() throws E;
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    public <T, E extends Exception> T execute(ThrowingSupplier<T, E> action, Function<Throwable, E> onExhausted)
            throws E {
        return action.get();
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    public <E extends Exception> void execute(ThrowingRunnable<E> action, Function<Throwable, E> onExhausted) throws E {
        action.run();
    }

    @Recover
    public <T, E extends Exception> T recover(
            Throwable e, ThrowingSupplier<T, E> action, java.util.function.Function<Throwable, E> onExhausted)
            throws E {

        if (e instanceof OptimisticLockingFailureException) {
            log.warn("optimistic retry exhausted", e);
            throw onExhausted.apply(e);
        }

        sneakyThrow(e);
        return null;
    }

    @Recover
    public <E extends Exception> void recover(
            Throwable e, ThrowingRunnable<E> action, java.util.function.Function<Throwable, E> onExhausted) throws E {

        if (e instanceof OptimisticLockingFailureException) {
            log.warn("optimistic retry exhausted", e);
            throw onExhausted.apply(e);
        }

        sneakyThrow(e);
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> void sneakyThrow(Throwable t) throws X {
        throw (X) t;
    }
}
