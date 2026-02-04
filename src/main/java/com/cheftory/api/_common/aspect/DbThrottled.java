package com.cheftory.api._common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DB 커넥션 풀 고갈을 방지하기 위해 동시 실행 수를 제한하는 어노테이션.
 * 주로 Virtual Thread 환경에서 무분별한 DB 접근을 제어하기 위해 사용함.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbThrottled {
}
