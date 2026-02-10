package com.cheftory.api._common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 사용자 Principal 주입 어노테이션.
 *
 * <p>Controller 메서드 파라미터에 사용하여 현재 인증된 사용자 정보를 주입받습니다.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserPrincipal {}
