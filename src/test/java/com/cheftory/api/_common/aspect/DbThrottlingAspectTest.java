package com.cheftory.api._common.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DbThrottlingAspect")
class DbThrottlingAspectTest {

    private DbThrottlingAspect sut;

    @BeforeEach
    void setUp() {
        sut = new DbThrottlingAspect();
    }

    @Test
    @DisplayName("동시에 여러 요청이 와도 세마포어에 의해 실행이 제어된다")
    void shouldThrottleConcurrentRequests() throws Throwable {
        int threadCount = 30;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger activeCount = new AtomicInteger(0);
        AtomicInteger maxActiveCount = new AtomicInteger(0);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            int current = activeCount.incrementAndGet();
            synchronized (maxActiveCount) {
                if (current > maxActiveCount.get()) {
                    maxActiveCount.set(current);
                }
            }
            Thread.sleep(50);
            activeCount.decrementAndGet();
            return null;
        });

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    sut.throttle(joinPoint);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(maxActiveCount.get()).isLessThanOrEqualTo(20);
        verify(joinPoint, times(threadCount)).proceed();
    }
}
