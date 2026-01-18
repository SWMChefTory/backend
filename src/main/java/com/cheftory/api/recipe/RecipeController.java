package com.cheftory.api.recipe;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.cheftory.api.recipe.creation.RecipeCreationFacade;
import com.cheftory.api.recipe.dto.CategorizedRecipesResponse;
import com.cheftory.api.recipe.dto.ChallengeRecipesResponse;
import com.cheftory.api.recipe.dto.CuisineRecipesResponse;
import com.cheftory.api.recipe.dto.FullRecipe;
import com.cheftory.api.recipe.dto.FullRecipeResponse;
import com.cheftory.api.recipe.dto.RecentRecipesResponse;
import com.cheftory.api.recipe.dto.RecipeCategoryCounts;
import com.cheftory.api.recipe.dto.RecipeCategoryCountsResponse;
import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.dto.RecipeCreateResponse;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeHistoryOverview;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeOverviewResponse;
import com.cheftory.api.recipe.dto.RecipeProgressResponse;
import com.cheftory.api.recipe.dto.RecipeProgressStatus;
import com.cheftory.api.recipe.dto.RecommendRecipesResponse;
import com.cheftory.api.recipe.dto.UnCategorizedRecipesResponse;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class RecipeController {
  private final RecipeFacade recipeFacade;
  private final RecipeCreationFacade recipeCreationFacade;

  @PostMapping("/api/v1/recipes")
  public RecipeCreateResponse create(
      @RequestBody RecipeCreateRequest request, @UserPrincipal UUID userId) {
    UUID recipeId = recipeCreationFacade.create(request.toUserTarget(userId));
    return RecipeCreateResponse.from(recipeId);
  }

  @GetMapping("/api/v1/recipes/{recipeId}")
  public FullRecipeResponse getFullRecipeResponse(
      @PathVariable("recipeId") UUID recipeId, @UserPrincipal UUID userId) {
    FullRecipe info = recipeFacade.viewFullRecipe(recipeId, userId);
    return FullRecipeResponse.of(info);
  }

  @GetMapping("/api/v1/recipes/overview/{recipeId}")
  public RecipeOverviewResponse getOverviewRecipeResponse(
      @PathVariable("recipeId") UUID recipeId, @UserPrincipal UUID userId) {
    RecipeOverview overview = recipeFacade.getRecipeOverview(recipeId, userId);
    return RecipeOverviewResponse.of(overview);
  }

  @GetMapping("/api/v1/recipes/recent")
  public RecentRecipesResponse getRecentInfos(
      @UserPrincipal UUID userId,
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(required = false) String cursor) {
    if (page != null) {
      Page<RecipeHistoryOverview> infos = recipeFacade.getRecents(userId, page);
      return RecentRecipesResponse.from(infos);
    }

    return RecentRecipesResponse.from(recipeFacade.getRecents(userId, cursor));
  }

  @GetMapping("/api/v1/recipes/recommend")
  @Deprecated
  public RecommendRecipesResponse getRecommendedRecipesDefault(
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query,
      @UserPrincipal UUID userId,
      @RequestParam(required = false) String cursor) {
    if (page != null) {
      return RecommendRecipesResponse.from(
          recipeFacade.getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, page, query));
    }

    return RecommendRecipesResponse.from(
        recipeFacade.getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, cursor, query));
  }

  @GetMapping("/api/v1/recipes/recommend/{type}")
  public RecommendRecipesResponse getRecommendedRecipes(
      @PathVariable String type,
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query,
      @UserPrincipal UUID userId,
      @RequestParam(required = false) String cursor) {
    RecipeInfoRecommendType recommendType = RecipeInfoRecommendType.fromString(type);

    if (page != null) {
      return RecommendRecipesResponse.from(
          recipeFacade.getRecommendRecipes(recommendType, userId, page, query));
    }

    return RecommendRecipesResponse.from(
        recipeFacade.getRecommendRecipes(recommendType, userId, cursor, query));
  }

  @GetMapping("/api/v1/recipes/categorized/{recipeCategoryId}")
  public CategorizedRecipesResponse getCategorizedRecipes(
      @PathVariable("recipeCategoryId") UUID categoryId,
      @UserPrincipal UUID userId,
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(required = false) String cursor) {
    if (page != null) {
      return CategorizedRecipesResponse.from(recipeFacade.getCategorized(userId, categoryId, page));
    }

    return CategorizedRecipesResponse.from(recipeFacade.getCategorized(userId, categoryId, cursor));
  }

  @GetMapping("/api/v1/recipes/uncategorized")
  public UnCategorizedRecipesResponse getUnCategorizedRecipes(
      @UserPrincipal UUID userId,
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(required = false) String cursor) {
    if (page != null) {
      Page<RecipeHistoryOverview> infos = recipeFacade.getUnCategorized(userId, page);
      return UnCategorizedRecipesResponse.from(infos);
    }

    return UnCategorizedRecipesResponse.from(recipeFacade.getUnCategorized(userId, cursor));
  }

  @DeleteMapping("/api/v1/recipes/categories/{recipeCategoryId}")
  public SuccessOnlyResponse deleteRecipeCategory(
      @PathVariable("recipeCategoryId") UUID recipeCategoryId) {
    recipeFacade.deleteCategory(recipeCategoryId);
    return SuccessOnlyResponse.create();
  }

  @GetMapping("/api/v1/recipes/categories")
  public RecipeCategoryCountsResponse getRecipeCategories(@UserPrincipal UUID userId) {
    RecipeCategoryCounts categories = recipeFacade.getUserCategoryCounts(userId);
    return RecipeCategoryCountsResponse.from(categories);
  }

  @GetMapping("/api/v1/recipes/progress/{recipeId}")
  public RecipeProgressResponse getRecipeProgress(@PathVariable("recipeId") UUID recipeId) {
    RecipeProgressStatus progressStatus = recipeFacade.getRecipeProgress(recipeId);
    return RecipeProgressResponse.of(progressStatus);
  }

  @PostMapping("/api/v1/recipes/block/{recipeId}")
  public SuccessOnlyResponse blockRecipe(@PathVariable UUID recipeId) {
    recipeFacade.blockRecipe(recipeId);
    return SuccessOnlyResponse.create();
  }

  @PostMapping("/papi/v1/recipes")
  public RecipeCreateResponse createCrawledRecipe(@RequestBody RecipeCreateRequest request) {
    UUID recipeId = recipeCreationFacade.create(request.toCrawlerTarget());
    return RecipeCreateResponse.from(recipeId);
  }

  @GetMapping("/papi/v1/recipes/progress/{recipeId}")
  public RecipeProgressResponse getCrawledRecipeProgress(@PathVariable("recipeId") UUID recipeId) {
    RecipeProgressStatus progressStatus = recipeFacade.getRecipeProgress(recipeId);
    return RecipeProgressResponse.of(progressStatus);
  }

  @GetMapping("/api/v1/recipes/cuisine/{type}")
  public CuisineRecipesResponse getBrowseRecipes(
      @PathVariable String type,
      @RequestParam(required = false) @Min(0) Integer page,
      @RequestParam(required = false) String cursor,
      @UserPrincipal UUID userId) {
    RecipeCuisineType cuisineType = RecipeCuisineType.fromString(type);

    if (page != null) {
      Page<RecipeOverview> recipes = recipeFacade.getCuisineRecipes(cuisineType, userId, page);
      return CuisineRecipesResponse.from(recipes);
    }

    CursorPage<RecipeOverview> recipes =
        recipeFacade.getCuisineRecipes(cuisineType, userId, cursor);
    return CuisineRecipesResponse.from(recipes);
  }

  @PocOnly(until = "2025-12-31")
  @GetMapping("/api/v1/recipes/challenge/{challengeId}")
  public ChallengeRecipesResponse getChallengeRecipes(
      @PathVariable UUID challengeId,
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @UserPrincipal UUID userId) {
    Pair<List<RecipeCompleteChallenge>, Page<RecipeOverview>> result =
        recipeFacade.getChallengeRecipes(challengeId, userId, page);
    return ChallengeRecipesResponse.from(result.getFirst(), result.getSecond());
  }
}
