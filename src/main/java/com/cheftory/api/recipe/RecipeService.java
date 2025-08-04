package com.cheftory.api.recipe;

import com.cheftory.api.recipe.category.RecipeCategory;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.CountRecipeCategory;
import com.cheftory.api.recipe.model.RecipeOverview;
import com.cheftory.api.recipe.model.RecipeHistoryOverview;
import com.cheftory.api.recipe.model.RecipeSort;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.util.YoutubeUrlNormalizer;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.client.VideoInfoClient;
import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusCount;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusService;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.util.List;
import java.util.UUID;
import org.springframework.web.util.UriComponentsBuilder;

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
  private final RecipeViewStatusService recipeViewStatusService;
  private final RecipeCategoryService recipeCategoryService;

  public UUID create(URI uri, UUID userId) {
    UriComponents uriOriginal = UriComponentsBuilder.fromUri(uri).build();
    UriComponents urlNormalized = youtubeUrlNormalizer.normalize(uriOriginal);
    return findByUrl(urlNormalized.toUri())
        .map(recipe -> {
          recipeViewStatusService.create(userId, recipe.getId());
          return recipe.getId();
        })
        .orElseGet(() -> {
          VideoInfo videoInfo = videoInfoClient.fetchVideoInfo(urlNormalized);
          UUID recipeId = recipeRepository.save(Recipe.preCompletedOf(videoInfo)).getId();
          asyncRecipeCreationService.create(recipeId);
          recipeViewStatusService.create(userId, recipeId);
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

  public FullRecipeInfo findFullRecipe(UUID recipeId, UUID userId) {
    Recipe recipe = recipeRepository.findById(recipeId)
        .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));

    if (recipe.isCompleted()) {
      recipeRepository.increaseCount(recipeId);
    }

    List<RecipeStep> recipeSteps = recipeStepService.findByRecipeId(recipeId);
    Optional<RecipeIngredients> ingredients = recipeIngredientsService.findByRecipeId(recipeId);

    RecipeViewStatus recipeViewStatus = recipeViewStatusService.find(userId, recipeId);

    log.info("Recipe {} loaded with {} steps and {} ingredients",
        recipeId, recipeSteps.size(), ingredients.isPresent() ? "some" : "no");

    return FullRecipeInfo.of(
        recipe.getStatus(),
        recipe.getVideoInfo(),
        ingredients.orElse(null),
        recipeSteps,
        recipeViewStatus
    );
  }

  public List<RecipeOverview> findRecommends() {
    return recipeRepository.findByStatus(RecipeStatus.COMPLETED, RecipeSort.COUNT_DESC)
        .stream()
        .map(RecipeOverview::from)
        .toList();
  }

  public List<RecipeHistoryOverview> findRecents(UUID userId) {
    List<RecipeViewStatus> viewStatuses = recipeViewStatusService.findRecentUsers(userId);
    return buildHistoryOverviews(viewStatuses);
  }

  public List<RecipeHistoryOverview> findCategorized(UUID userId, UUID recipeCategoryId) {
    List<RecipeViewStatus> viewStatuses = recipeViewStatusService.findCategories(userId, recipeCategoryId);
    return buildHistoryOverviews(viewStatuses);
  }

  public List<RecipeHistoryOverview> findUnCategorized(UUID userId) {
    List<RecipeViewStatus> viewStatuses = recipeViewStatusService.findUnCategories(userId);
    return buildHistoryOverviews(viewStatuses);
  }

  private List<RecipeHistoryOverview> buildHistoryOverviews(List<RecipeViewStatus> viewStatuses) {
    Map<UUID, RecipeViewStatus> viewStatusMap = viewStatuses.stream()
        .collect(Collectors.toMap(RecipeViewStatus::getRecipeId, Function.identity()));

    return recipeRepository
        .findRecipesByIdInAndStatus(viewStatusMap.keySet().stream().toList(), RecipeStatus.COMPLETED)
        .stream()
        .map(recipe -> RecipeHistoryOverview.of(recipe, viewStatusMap.get(recipe.getId())))
        .toList();
  }

  public List<CountRecipeCategory> findCategories(UUID userId) {
    var categories = recipeCategoryService.findUsers(userId);
    var categoryIds = categories.stream().map(RecipeCategory::getId).toList();
    var counts = recipeViewStatusService.countByCategories(categoryIds);

    var countMap = counts.stream()
        .collect(Collectors.toMap(RecipeViewStatusCount::getCategoryId, RecipeViewStatusCount::getCount));

    return categories.stream()
        .map(category -> CountRecipeCategory.of(category, countMap.getOrDefault(category.getId(), 0)))
        .toList();
  }

  public void deleteCategory(UUID categoryId) {
    recipeViewStatusService.deleteCategories(categoryId);
    recipeCategoryService.delete(categoryId);
  }

}
