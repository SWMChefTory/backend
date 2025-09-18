package com.cheftory.api.recipe.category.validator;


import com.cheftory.api.account.user.UserService;
import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCategoryIdValidator implements ConstraintValidator<ExistsRecipeCategoryId, UUID> {

  private final RecipeCategoryService service;

  @Override
  public boolean isValid(UUID userId, ConstraintValidatorContext context) {
    boolean exists = service.exists(userId);

    if (!exists) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND.name())
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}