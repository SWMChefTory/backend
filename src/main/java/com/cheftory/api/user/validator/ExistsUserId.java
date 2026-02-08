package com.cheftory.api.user.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 유저 ID 존재 여부 검증 어노테이션
 *
 * <p>해당 유저 ID가 시스템에 존재하는지 검증합니다.</p>
 */
@Documented
@Constraint(validatedBy = UserIdValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsUserId {
    /**
     * 검증 실패 시 메시지
     *
     * @return 에러 메시지
     */
    String message() default "존재하지 않는 유저 ID입니다.";

    /**
     * 검증 그룹
     *
     * @return 그룹 클래스 배열
     */
    Class<?>[] groups() default {};

    /**
     * 페이로드
     *
     * @return 페이로드 클래스 배열
     */
    Class<? extends Payload>[] payload() default {};
}
