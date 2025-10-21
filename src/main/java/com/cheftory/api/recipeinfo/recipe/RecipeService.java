package com.cheftory.api.recipeinfo.recipe;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.model.RecipeSort;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeException;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
  private final RecipeRepository recipeRepository;
  private final Clock clock;

  public Recipe getSuccess(UUID recipeId) {

    Recipe recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));

    if (recipe.isFailed()) {
      throw new RecipeException(RecipeErrorCode.RECIPE_FAILED);
    }

    recipeRepository.increaseCount(recipeId);
    return recipe;
  }

  public Recipe getNotFailed(List<UUID> recipeIds) {
    List<Recipe> recipes = recipeRepository.findAllByIdIn(recipeIds);

    if (recipes.isEmpty()) {
      throw new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND);
    }

    if (recipes.stream().allMatch(Recipe::isFailed)) {
      throw new RecipeException(RecipeErrorCode.RECIPE_FAILED);
    }

    List<Recipe> validRecipes = recipes.stream().filter(r -> !r.isFailed()).toList();

    if (validRecipes.size() > 1) {
      log.warn(
          "여러 개의 progress 및 success 상태 Recipe가 조회되었습니다. recipeIds={}, count={}",
          recipeIds,
          validRecipes.size());
    }

    return validRecipes.getFirst();
  }

  public UUID create() {
    Recipe recipe = Recipe.create(clock);
    recipeRepository.save(recipe);
    return recipe.getId();
  }

  public List<Recipe> getValidRecipes(List<UUID> recipeIds) {
    return recipeRepository
        .findRecipesByIdInAndRecipeStatusIn(
            recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS))
        .stream()
        .toList();
  }

  public List<Recipe> gets(List<UUID> recipeIds) {
    return recipeRepository.findAllByIdIn(recipeIds);
  }

  public Page<Recipe> getPopulars(Integer page) {
    Pageable pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);
    return recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);
  }

  public Recipe success(UUID recipeId) {
    Recipe recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));
    recipe.success(clock);
    return recipeRepository.save(recipe);
  }

  public Recipe failed(UUID recipeId) {
    Recipe recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));
    recipe.failed(clock);
    return recipeRepository.save(recipe);
  }

  public boolean exists(UUID recipeId) {
    return recipeRepository.existsById(recipeId);
  }

  public Recipe get(UUID recipeId) {
    return recipeRepository
        .findById(recipeId)
        .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));
  }
}
