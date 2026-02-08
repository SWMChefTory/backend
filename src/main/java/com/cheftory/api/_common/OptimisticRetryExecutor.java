package com.cheftory.api._common;

import java.util.function.Supplier;
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

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }

    @Recover
    public <T> T recover(Throwable e, Supplier<T> action) {
        log.warn("optimistic retry exhausted", e);
        throw new IllegalStateException("optimistic retry exhausted", e);
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    public void execute(Runnable action) {
        action.run();
    }

    @Recover
    public void recover(Throwable e, Runnable action) {
        log.warn("optimistic retry exhausted", e);
        throw new IllegalStateException("optimistic retry exhausted", e);
    }

    public <T> T execute(Supplier<T> action, Supplier<? extends RuntimeException> onExhausted) {
        try {
            return execute(action);
        } catch (IllegalStateException e) {
            throw onExhausted.get();
        }
    }

    public void execute(Runnable action, Supplier<? extends RuntimeException> onExhausted) {
        try {
            execute(action);
        } catch (IllegalStateException e) {
            throw onExhausted.get();
        }
    }
}
