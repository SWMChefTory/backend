package com.cheftory.api.recipeinfo;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipeinfo.model.CategorizedRecipesResponse;
import com.cheftory.api.recipeinfo.model.ChefRecipesResponse;
import com.cheftory.api.recipeinfo.model.FullRecipe;
import com.cheftory.api.recipeinfo.model.FullRecipeResponse;
import com.cheftory.api.recipeinfo.model.RecentRecipesResponse;
import com.cheftory.api.recipeinfo.model.RecipeCategoryCounts;
import com.cheftory.api.recipeinfo.model.RecipeCategoryCountsResponse;
import com.cheftory.api.recipeinfo.model.RecipeCreateRequest;
import com.cheftory.api.recipeinfo.model.RecipeCreateResponse;
import com.cheftory.api.recipeinfo.model.RecipeHistoriesResponse;
import com.cheftory.api.recipeinfo.model.RecipeHistoryOverview;
import com.cheftory.api.recipeinfo.model.RecipeInfoVideoQuery;
import com.cheftory.api.recipeinfo.model.RecipeOverview;
import com.cheftory.api.recipeinfo.model.RecipeProgressResponse;
import com.cheftory.api.recipeinfo.model.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.model.RecommendRecipesResponse;
import com.cheftory.api.recipeinfo.model.SearchedRecipesResponse;
import com.cheftory.api.recipeinfo.model.TrendRecipesResponse;
import com.cheftory.api.recipeinfo.model.UnCategorizedRecipesResponse;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("")
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
    FullRecipe info = recipeInfoService.getFullRecipe(recipeId, userId);
    return FullRecipeResponse.of(info);
  }

  @GetMapping("/api/v1/recipes/recent")
  public RecentRecipesResponse getRecentInfos(
      @UserPrincipal UUID userId, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistoryOverview> infos = recipeInfoService.getRecents(userId, page);
    return RecentRecipesResponse.from(infos);
  }

  @GetMapping("/api/v1/recipes/recommend")
  public RecommendRecipesResponse getRecommendedRecipes(
      @RequestParam(defaultValue = "0") @Min(0) Integer page,
      @UserPrincipal UUID userId,
      @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query) {
    Page<RecipeOverview> recipes = recipeInfoService.getPopulars(page, userId, query);
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

  @GetMapping("/api/v1/recipes/histories")
  public RecipeHistoriesResponse getRecipeHistories(
      @UserPrincipal UUID userId, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistoryOverview> infos = recipeInfoService.getHistories(userId, page);
    return RecipeHistoriesResponse.from(infos);
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

  @GetMapping("/api/v1/recipes/trending")
  public TrendRecipesResponse getTrendingRecipes(
      @RequestParam(defaultValue = "0") @Min(0) Integer page, @UserPrincipal UUID userId) {
    Page<RecipeOverview> recipes = recipeInfoService.getTrendRecipes(userId, page);
    return TrendRecipesResponse.from(recipes);
  }

  @GetMapping("/api/v1/recipes/chef")
  public ChefRecipesResponse getChefRecipes(
      @RequestParam(defaultValue = "0") @Min(0) Integer page, @UserPrincipal UUID userId) {
    Page<RecipeOverview> recipes = recipeInfoService.getChefRecipes(userId, page);
    return ChefRecipesResponse.from(recipes);
  }
}
