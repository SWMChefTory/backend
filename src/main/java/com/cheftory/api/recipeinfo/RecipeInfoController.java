package com.cheftory.api.recipeinfo;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipeinfo.challenge.RecipeCompleteChallenge;
import com.cheftory.api.recipeinfo.dto.CategorizedRecipesResponse;
import com.cheftory.api.recipeinfo.dto.ChallengeRecipesResponse;
import com.cheftory.api.recipeinfo.dto.CuisineRecipesResponse;
import com.cheftory.api.recipeinfo.dto.FullRecipe;
import com.cheftory.api.recipeinfo.dto.FullRecipeResponse;
import com.cheftory.api.recipeinfo.dto.RecentRecipesResponse;
import com.cheftory.api.recipeinfo.dto.RecipeCategoryCounts;
import com.cheftory.api.recipeinfo.dto.RecipeCategoryCountsResponse;
import com.cheftory.api.recipeinfo.dto.RecipeCreateRequest;
import com.cheftory.api.recipeinfo.dto.RecipeCreateResponse;
import com.cheftory.api.recipeinfo.dto.RecipeHistoryOverview;
import com.cheftory.api.recipeinfo.dto.RecipeInfoCuisineType;
import com.cheftory.api.recipeinfo.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipeinfo.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipeinfo.dto.RecipeOverview;
import com.cheftory.api.recipeinfo.dto.RecipeOverviewResponse;
import com.cheftory.api.recipeinfo.dto.RecipeProgressResponse;
import com.cheftory.api.recipeinfo.dto.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.dto.RecommendRecipesResponse;
import com.cheftory.api.recipeinfo.dto.SearchedRecipesResponse;
import com.cheftory.api.recipeinfo.dto.UnCategorizedRecipesResponse;
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
public class RecipeInfoController {
  private final RecipeInfoService recipeInfoService;

  @PostMapping("/api/v1/recipes")
  public RecipeCreateResponse create(
      @RequestBody RecipeCreateRequest request, @UserPrincipal UUID userId) {
    UUID recipeId = recipeInfoService.create(request.toUserTarget(userId));
    return RecipeCreateResponse.from(recipeId);
  }

  @GetMapping("/api/v1/recipes/{recipeId}")
  public FullRecipeResponse getFullRecipeResponse(
      @PathVariable("recipeId") UUID recipeId, @UserPrincipal UUID userId) {
    FullRecipe info = recipeInfoService.viewFullRecipe(recipeId, userId);
    return FullRecipeResponse.of(info);
  }

  @GetMapping("/api/v1/recipes/overview/{recipeId}")
  public RecipeOverviewResponse getOverviewRecipeResponse(
      @PathVariable("recipeId") UUID recipeId, @UserPrincipal UUID userId) {
    RecipeOverview overview = recipeInfoService.getRecipeOverview(recipeId, userId);
    return RecipeOverviewResponse.of(overview);
  }

  @GetMapping("/api/v1/recipes/recent")
  public RecentRecipesResponse getRecentInfos(
      @UserPrincipal UUID userId, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistoryOverview> infos = recipeInfoService.getRecents(userId, page);
    return RecentRecipesResponse.from(infos);
  }

  @GetMapping("/api/v1/recipes/recommend")
  @Deprecated
  public RecommendRecipesResponse getRecommendedRecipesDefault(
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query,
      @UserPrincipal UUID userId) {
    Page<RecipeOverview> recipes =
        recipeInfoService.getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, page, query);
    return RecommendRecipesResponse.from(recipes);
  }

  @GetMapping("/api/v1/recipes/recommend/{type}")
  public RecommendRecipesResponse getRecommendedRecipes(
      @PathVariable String type,
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query,
      @UserPrincipal UUID userId) {
    RecipeInfoRecommendType recommendType = RecipeInfoRecommendType.fromString(type);
    Page<RecipeOverview> recipes =
        recipeInfoService.getRecommendRecipes(recommendType, userId, page, query);
    return RecommendRecipesResponse.from(recipes);
  }

  @GetMapping("/api/v1/recipes/categorized/{recipeCategoryId}")
  public CategorizedRecipesResponse getCategorizedRecipes(
      @PathVariable("recipeCategoryId") UUID categoryId,
      @UserPrincipal UUID userId,
      @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistoryOverview> infos = recipeInfoService.getCategorized(userId, categoryId, page);
    return CategorizedRecipesResponse.from(infos);
  }

  @GetMapping("/api/v1/recipes/uncategorized")
  public UnCategorizedRecipesResponse getUnCategorizedRecipes(
      @UserPrincipal UUID userId, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistoryOverview> infos = recipeInfoService.getUnCategorized(userId, page);
    return UnCategorizedRecipesResponse.from(infos);
  }

  @DeleteMapping("/api/v1/recipes/categories/{recipeCategoryId}")
  public SuccessOnlyResponse deleteRecipeCategory(
      @PathVariable("recipeCategoryId") UUID recipeCategoryId) {
    recipeInfoService.deleteCategory(recipeCategoryId);
    return SuccessOnlyResponse.create();
  }

  @GetMapping("/api/v1/recipes/categories")
  public RecipeCategoryCountsResponse getRecipeCategories(@UserPrincipal UUID userId) {
    RecipeCategoryCounts categories = recipeInfoService.getCategoryCounts(userId);
    return RecipeCategoryCountsResponse.from(categories);
  }

  @GetMapping("/api/v1/recipes/progress/{recipeId}")
  public RecipeProgressResponse getRecipeProgress(@PathVariable("recipeId") UUID recipeId) {
    RecipeProgressStatus progressStatus = recipeInfoService.getRecipeProgress(recipeId);
    return RecipeProgressResponse.of(progressStatus);
  }

  @GetMapping("/api/v1/recipes/search")
  public SearchedRecipesResponse searchRecipes(
      @RequestParam("query") String query,
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @UserPrincipal UUID userId) {
    Page<RecipeOverview> recipes = recipeInfoService.searchRecipes(page, query, userId);
    return SearchedRecipesResponse.from(recipes);
  }

  @PostMapping("/api/v1/recipes/block/{recipeId}")
  public SuccessOnlyResponse blockRecipe(@PathVariable UUID recipeId) {
    recipeInfoService.blockRecipe(recipeId);
    return SuccessOnlyResponse.create();
  }

  @PostMapping("/papi/v1/recipes")
  public RecipeCreateResponse createCrawledRecipe(@RequestBody RecipeCreateRequest request) {
    UUID recipeId = recipeInfoService.create(request.toCrawlerTarget());
    return RecipeCreateResponse.from(recipeId);
  }

  @GetMapping("/papi/v1/recipes/progress/{recipeId}")
  public RecipeProgressResponse getCrawledRecipeProgress(@PathVariable("recipeId") UUID recipeId) {
    RecipeProgressStatus progressStatus = recipeInfoService.getRecipeProgress(recipeId);
    return RecipeProgressResponse.of(progressStatus);
  }

  @GetMapping("/api/v1/recipes/cuisine/{type}")
  public CuisineRecipesResponse getBrowseRecipes(
      @PathVariable String type,
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @UserPrincipal UUID userId) {
    RecipeInfoCuisineType cuisineType = RecipeInfoCuisineType.fromString(type);
    Page<RecipeOverview> recipes = recipeInfoService.getCuisineRecipes(cuisineType, userId, page);
    return CuisineRecipesResponse.from(recipes);
  }

  @PocOnly(until = "2025-12-31")
  @GetMapping("/api/v1/recipes/challenge/{challengeId}")
  public ChallengeRecipesResponse getChallengeRecipes(
      @PathVariable UUID challengeId,
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @UserPrincipal UUID userId) {
    Pair<List<RecipeCompleteChallenge>, Page<RecipeOverview>> result =
        recipeInfoService.getChallengeRecipes(challengeId, userId, page);
    return ChallengeRecipesResponse.from(result.getFirst(), result.getSecond());
  }
}
