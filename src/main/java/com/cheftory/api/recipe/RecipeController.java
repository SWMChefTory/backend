package com.cheftory.api.recipe;

import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.model.RecentRecipeOverview;
import com.cheftory.api.recipe.dto.FullRecipeResponse;
import com.cheftory.api.recipe.dto.RecipeCreateResponse;
import com.cheftory.api.recipe.dto.RecentRecipesResponse;
import com.cheftory.api.security.UserPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipe")
public class RecipeController {
  private final RecipeService recipeService;

  @PostMapping("")
  public RecipeCreateResponse create(@RequestBody RecipeCreateRequest recipeCreateRequest, @UserPrincipal UUID userId) {
    UUID recipeId = recipeService.create(recipeCreateRequest.videoUrl(),userId);
    return RecipeCreateResponse.from(recipeId);
  }

  @GetMapping("/{recipe_id}")
  public FullRecipeResponse getFullRecipeResponse(@PathVariable("recipe_id") UUID recipe_id, @UserPrincipal UUID userId) {
    FullRecipeInfo info = recipeService.findFullRecipe(recipe_id,userId);
    return FullRecipeResponse.of(info);
  }

  @GetMapping("")
  public RecentRecipesResponse getRecentInfos(@UserPrincipal UUID userId) {
    List<RecentRecipeOverview> infos = recipeService.findUsers(userId);
    return RecentRecipesResponse.from(infos);
  }
}
