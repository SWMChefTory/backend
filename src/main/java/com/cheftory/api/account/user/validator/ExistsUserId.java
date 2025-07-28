package com.cheftory.api.account.user.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Constraint(validatedBy = UserIdValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsUserId {
  String message() default "존재하지 않는 레시피 ID입니다.";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}