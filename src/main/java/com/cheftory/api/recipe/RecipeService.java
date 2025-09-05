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
import com.cheftory.api.recipe.util.RecipePageRequest;
import com.cheftory.api.recipe.util.YoutubeUrlNormalizer;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.client.VideoInfoClient;
import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusCount;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusService;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  public Page<RecipeOverview> findRecommends(Integer page) {
    Pageable pageable = RecipePageRequest.create(page,RecipeSort.COUNT_DESC);
    return recipeRepository.findByStatus(RecipeStatus.COMPLETED, pageable)
        .map(RecipeOverview::from);
  }

  public Page<RecipeHistoryOverview> findRecents(UUID userId, Integer page) {
    Page<RecipeViewStatus> viewStatuses = recipeViewStatusService.findRecentUsers(userId, page);
    return buildHistoryOverviews(viewStatuses);
  }

  public Page<RecipeHistoryOverview> findCategorized(UUID userId, UUID recipeCategoryId, Integer page) {
    Page<RecipeViewStatus> viewStatuses = recipeViewStatusService.findCategories(userId, recipeCategoryId, page);
    return buildHistoryOverviews(viewStatuses);
  }

  public Page<RecipeHistoryOverview> findUnCategorized(UUID userId, Integer page) {
    Page<RecipeViewStatus> viewStatuses = recipeViewStatusService.findUnCategories(userId, page);
    return buildHistoryOverviews(viewStatuses);
  }

  private Page<RecipeHistoryOverview> buildHistoryOverviews(Page<RecipeViewStatus> viewStatuses) {
    List<UUID> recipeIds = viewStatuses.stream()
        .map(RecipeViewStatus::getRecipeId)
        .toList();

    Map<UUID, Recipe> recipeMap = recipeRepository
        .findRecipesByIdInAndStatus(recipeIds, RecipeStatus.COMPLETED)
        .stream()
        .collect(Collectors.toMap(Recipe::getId, Function.identity()));

    List<RecipeHistoryOverview> content = viewStatuses.stream()
        .filter(viewStatus -> recipeMap.containsKey(viewStatus.getRecipeId()))
        .map(viewStatus -> RecipeHistoryOverview.of(recipeMap.get(viewStatus.getRecipeId()), viewStatus))
        .toList();

    return new PageImpl<>(content, viewStatuses.getPageable(), viewStatuses.getTotalElements());
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
