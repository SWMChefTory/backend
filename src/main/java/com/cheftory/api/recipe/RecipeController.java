package com.cheftory.api.recipe;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.model.CategorizedRecipesResponse;
import com.cheftory.api.recipe.model.CountRecipeCategoriesResponse;
import com.cheftory.api.recipe.model.CountRecipeCategory;
import com.cheftory.api.recipe.model.RecommendRecipesResponse;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecipeCreateRequest;
import com.cheftory.api.recipe.model.RecipeHistoryOverview;
import com.cheftory.api.recipe.model.FullRecipeResponse;
import com.cheftory.api.recipe.model.RecipeCreateResponse;
import com.cheftory.api.recipe.model.RecentRecipesResponse;
import com.cheftory.api.recipe.model.RecipeOverview;
import com.cheftory.api.recipe.model.UnCategorizedRecipesResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public RecentRecipesResponse getRecentInfos(@UserPrincipal UUID userId) {
    List<RecipeHistoryOverview> infos = recipeService.findRecents(userId);
    return RecentRecipesResponse.from(infos);
  }

  @GetMapping("/recommend")
  public RecommendRecipesResponse getRecommendedRecipes() {
    List<RecipeOverview> infos = recipeService.findRecommends();
    return RecommendRecipesResponse.from(infos);
  }

  @GetMapping("/categorized/{recipe_category_id}")
  public CategorizedRecipesResponse getCategorizedRecipes(@PathVariable("recipe_category_id") UUID categoryId, @UserPrincipal UUID userId) {
    List<RecipeHistoryOverview> infos = recipeService.findCategorized(userId,categoryId);
    return CategorizedRecipesResponse.from(infos);
  }

  @GetMapping("/uncategorized")
  public UnCategorizedRecipesResponse getUnCategorizedRecipes(@UserPrincipal UUID userId) {
    List<RecipeHistoryOverview> infos = recipeService.findUnCategorized(userId);
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
