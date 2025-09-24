package com.cheftory.api.recipeinfo.category;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipeinfo.category.exception.RecipeCategoryException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCategoryService {

  private final RecipeCategoryRepository recipeCategoryRepository;
  private final Clock clock;

  public UUID create(String name, UUID userId) {
    RecipeCategory recipeCategory = RecipeCategory.create(clock, name, userId);
    return recipeCategoryRepository.save(recipeCategory).getId();
  }

  @Transactional
  public void delete(UUID recipeCategoryId) {
    RecipeCategory recipeCategory =
        recipeCategoryRepository
            .findById(recipeCategoryId)
            .orElseThrow(
                () ->
                    new RecipeCategoryException(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND));
    recipeCategory.delete();
  }

  public List<RecipeCategory> findUsers(UUID userId) {
    return recipeCategoryRepository.findAllByUserIdAndStatus(userId, RecipeCategoryStatus.ACTIVE);
  }

  public boolean exists(UUID recipeCategoryId) {
    return recipeCategoryRepository.existsById(recipeCategoryId);
  }
}
