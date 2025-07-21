package com.cheftory.api.recipe;

import com.cheftory.api.recipe.dto.*;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.repository.RecipeRepository;
import com.cheftory.api.recipe.service.YoutubeUrlNormalizer;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.client.VideoInfoClient;
import com.cheftory.api.recipe.service.AsyncRecipeCreationService;
import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

  private final VideoInfoClient videoInfoClient;
  private final AsyncRecipeCreationService asyncRecipeCreationService;
  private final RecipeRepository recipeRepository;
  private final RecipeStepService recipeStepService;
  private final RecipeIngredientsService recipeIngredientsService;
  private final YoutubeUrlNormalizer youtubeUrlNormalizer;

  public UUID create(UriComponents uri) {
    UriComponents urlNormalized = youtubeUrlNormalizer.normalize(uri);
    return findByUrl(urlNormalized.toUri())
        .map(Recipe::getId)
        .orElseGet(() -> {
          VideoInfo videoInfo = videoInfoClient.fetchVideoInfo(urlNormalized);
          UUID recipeId = recipeRepository.save(Recipe.preCompletedOf(videoInfo)).getId();
          asyncRecipeCreationService.create(recipeId);
          return recipeId;
        });
  }

  private Optional<Recipe> findByUrl(URI url) {
    List<Recipe> recipes = recipeRepository.findAllByVideoUrl(url);

    return recipes.stream()
        .filter(r -> r.isCompleted() || r.isReady())
        .findFirst()
        .or(() -> {
          recipes.stream()
              .filter(Recipe::isBanned)
              .findFirst()
              .ifPresent(r -> {
                throw new RecipeException(RecipeErrorCode.RECIPE_BANNED);
              });
          return Optional.empty();
        });
  }

  private Recipe findById(UUID recipeId) {
    return recipeRepository.findById(recipeId)
        .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));
  }

  public FullRecipeInfo findFullRecipeInfo(UUID recipeId) {
    Recipe recipe = findById(recipeId);

    List<RecipeStepInfo> recipeInfos = recipeStepService
        .getRecipeStepInfos(recipeId);
    IngredientsInfo ingredientsInfo = recipeIngredientsService
        .findIngredientsInfoOfRecipe(recipeId);

    if (!recipeInfos.isEmpty() && Objects.nonNull(ingredientsInfo)) {
      recipeRepository.increaseCount(recipeId);
    }

    return FullRecipeInfo.of(
        recipe.getStatus()
        , recipe.getVideoInfo()
        , ingredientsInfo, recipeInfos);
  }

  public List<RecipeOverview> findOverviewRecipes(List<UUID> recipeIds) {
    return recipeRepository.findRecipesById(recipeIds)
        .stream()
        .filter(Recipe::isCompleted)
        .map(RecipeOverview::from)
        .toList();
  }
}
