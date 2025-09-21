package com.cheftory.api.recipeinfo.recipe.validator;

import com.cheftory.api.recipeinfo.recipe.RecipeService;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeIdValidator implements ConstraintValidator<ExistsRecipeId, UUID> {

  private final RecipeService recipeService;

  @Override
  public boolean isValid(UUID userId, ConstraintValidatorContext context) {
    boolean exists = recipeService.exists(userId);

    if (!exists) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(RecipeErrorCode.RECIPE_NOT_FOUND.name())
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
