package com.cheftory.api.recipeinfo.recipe.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = RecipeIdValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsRecipeId {
  String message() default "존재하지 않는 레시피 ID입니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
