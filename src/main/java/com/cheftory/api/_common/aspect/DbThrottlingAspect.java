package com.cheftory.api._common.aspect;

import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DbThrottlingAspect {

    private final Semaphore semaphore = new Semaphore(20);

    @Around("@annotation(com.cheftory.api._common.aspect.DbThrottled)")
    public Object throttle(ProceedingJoinPoint joinPoint) throws Throwable {
        semaphore.acquire();
        try {
            return joinPoint.proceed();
        } finally {
            semaphore.release();
        }
    }
}
