package com.cheftory.api.recipe;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.model.CategorizedRecipesResponse;
import com.cheftory.api.recipe.model.CountRecipeCategoriesResponse;
import com.cheftory.api.recipe.model.CountRecipeCategory;
import com.cheftory.api.recipe.model.RecommendRecipesResponse;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecipeCreateRequest;
import com.cheftory.api.recipe.model.RecipeHistory;
import com.cheftory.api.recipe.model.FullRecipeResponse;
import com.cheftory.api.recipe.model.RecipeCreateResponse;
import com.cheftory.api.recipe.model.RecentRecipesResponse;
import com.cheftory.api.recipe.model.UnCategorizedRecipesResponse;
import jakarta.validation.constraints.Min;
import java.util.List;
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
@RequestMapping("/api/v1/recipes")
public class RecipeController {
  private final RecipeService recipeService;

  @PostMapping("")
  public RecipeCreateResponse create(@RequestBody RecipeCreateRequest recipeCreateRequest, @UserPrincipal UUID userId) {
    UUID recipeId = recipeService.create(recipeCreateRequest.videoUrl(),userId);
    return RecipeCreateResponse.from(recipeId);
  }

  @GetMapping("/{recipe_id}")
  public FullRecipeResponse getFullRecipeResponse(@PathVariable("recipe_id") UUID recipeId, @UserPrincipal UUID userId) {
    FullRecipeInfo info = recipeService.findFullRecipe(recipeId,userId);
    return FullRecipeResponse.of(info);
  }

  @GetMapping("/recent")
  public RecentRecipesResponse getRecentInfos(
      @UserPrincipal UUID userId,
      @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistory> infos = recipeService.findRecents(userId, page);
    return RecentRecipesResponse.from(infos);
  }

  @GetMapping("/recommend")
  public RecommendRecipesResponse getRecommendedRecipes(
      @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<Recipe> recipes = recipeService.findRecommends(page);
    return RecommendRecipesResponse.from(recipes);
  }

  @GetMapping("/categorized/{recipe_category_id}")
  public CategorizedRecipesResponse getCategorizedRecipes(
      @PathVariable("recipe_category_id") UUID categoryId,
      @UserPrincipal UUID userId, @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistory> infos = recipeService.findCategorized(userId,categoryId, page);
    return CategorizedRecipesResponse.from(infos);
  }

  @GetMapping("/uncategorized")
  public UnCategorizedRecipesResponse getUnCategorizedRecipes(
      @UserPrincipal UUID userId,
      @RequestParam(defaultValue = "0") @Min(0) Integer page) {
    Page<RecipeHistory> infos = recipeService.findUnCategorized(userId, page);
    return UnCategorizedRecipesResponse.from(infos);
  }

  @DeleteMapping("/categories/{recipe_category_id}")
  public SuccessOnlyResponse deleteRecipeCategory(
      @PathVariable("recipe_category_id") UUID recipeCategoryId) {
    recipeService.deleteCategory(recipeCategoryId);
    return SuccessOnlyResponse.create();
  }

  @GetMapping("/categories")
  public CountRecipeCategoriesResponse getRecipeCategories(@UserPrincipal UUID userId) {
    List<CountRecipeCategory> categories = recipeService.findCategories(userId);
    return CountRecipeCategoriesResponse.from(categories);
  }
}
