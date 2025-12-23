package com.cheftory.api.recipe.content.info.validator;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeIdValidator implements ConstraintValidator<ExistsRecipeId, UUID> {

  private final RecipeInfoService recipeInfoService;

  @Override
  public boolean isValid(UUID recipeId, ConstraintValidatorContext context) {
    boolean exists = recipeInfoService.exists(recipeId);

    if (!exists) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.name())
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
