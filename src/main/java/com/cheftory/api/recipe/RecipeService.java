package com.cheftory.api.recipe;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecipeOverview;
import com.cheftory.api.recipe.model.RecentRecipeOverview;
import com.cheftory.api.recipe.repository.RecipeRepository;
import com.cheftory.api.recipe.service.YoutubeUrlNormalizer;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.client.VideoInfoClient;
import com.cheftory.api.recipe.service.AsyncRecipeCreationService;
import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusInfo;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusService;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

  public UUID create(URI uri, UUID userId) {
    UriComponents uriOriginal = UriComponentsBuilder.fromUri(uri).build();
    UriComponents urlNormalized = youtubeUrlNormalizer.normalize(uriOriginal);
    return findByUrl(urlNormalized.toUri())
        .map(Recipe::getId)
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

  private Recipe find(UUID recipeId) {
    return recipeRepository.findById(recipeId)
        .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));
  }

  public FullRecipeInfo findFullRecipe(UUID recipeId, UUID userId) {

    Recipe recipe = find(recipeId);

    List<RecipeStepInfo> recipeInfos = recipeStepService
        .getRecipeStepInfos(recipeId);

    if(recipeInfos.isEmpty()) {
      recipeInfos = null;
    }

    IngredientsInfo ingredientsInfo = recipeIngredientsService
        .findIngredientsInfoOfRecipe(recipeId);


    if (!Objects.requireNonNull(recipeInfos).isEmpty() && Objects.nonNull(ingredientsInfo)) {
      recipeRepository.increaseCount(recipeId);
    }

    RecipeViewStatusInfo recipeViewStatusInfo = recipeViewStatusService.find(userId, recipeId);

    return FullRecipeInfo.of(
        recipe.getStatus()
        , recipe.getVideoInfo()
        , ingredientsInfo, recipeInfos
        , recipeViewStatusInfo
    );
  }

  public List<RecentRecipeOverview> findUsers(UUID userId) {

    List<RecipeViewStatusInfo> viewStatusInfos = recipeViewStatusService.findUsers(userId);

    List<UUID> recipeIds = viewStatusInfos.stream()
        .map(RecipeViewStatusInfo::getRecipeId)
        .toList();

    List<RecipeOverview> recipeOverviews = recipeRepository.findRecipesById(recipeIds)
        .stream()
        .filter(Recipe::isCompleted)
        .map(RecipeOverview::from)
        .toList();

    Map<UUID, RecipeViewStatusInfo> viewStatusMap = viewStatusInfos.stream()
        .collect(Collectors.toMap(
            RecipeViewStatusInfo::getRecipeId,
            Function.identity(),
            (a, b) -> b,
            HashMap::new
        ));

    return recipeOverviews.stream()
        .map(recipe -> RecentRecipeOverview.of(
            recipe,
            viewStatusMap.get(recipe.getId())
        ))
        .sorted((a, b) -> b.getRecipeViewStatusInfo().getViewedAt()
            .compareTo(a.getRecipeViewStatusInfo().getViewedAt()))
        .toList();
  }
}
