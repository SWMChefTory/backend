package com.cheftory.api._common.aspect;

import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 쓰로틀링 AOP 애스팩트.
 *
 * <p>세마포어를 사용하여 동시 DB 연결 수를 제한합니다.</p>
 */
@Aspect
@Component
@Slf4j
public class DbThrottlingAspect {

    /** 최대 동시 DB 연결 수 */
    private final Semaphore semaphore = new Semaphore(20);

    /**
     * DB 쓰로틀링을 적용합니다.
     *
     * @param joinPoint 조인 포인트
     * @return 메서드 실행 결과
     * @throws Throwable 실행 중 예외 발생 시
     */
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
