package com.cheftory.api.recipe;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecentRecipeOverview;
import com.cheftory.api.recipe.model.RecipeOverview;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.web.util.UriComponentsBuilder;

public class RecipeControllerTestData {

  public static UUID userId = UUID.randomUUID();
  public static UUID recipeId = UUID.randomUUID();
  public static VideoInfo videoInfo = VideoInfo.from(
      UriComponentsBuilder.fromUriString("https://example.com/video?v=12345").build(),
      "Sample Recipe Video",
      URI.create("https://example.com/thumbnail.jpg"),
      120
  );
  public static Recipe recipe = Recipe.preCompletedOf(videoInfo);
  public static RecipeOverview recipeOverview = RecipeOverview.from(recipe);
  public static RecipeViewStatus recipeViewStatus = RecipeViewStatus.of(new Clock(),userId, recipeId);
  public static RecipeViewStatusInfo recipeViewStatusInfo = RecipeViewStatusInfo.of(recipeViewStatus);
  public static RecentRecipeOverview recentRecipeOverview = RecentRecipeOverview.of(
      recipeOverview,
      recipeViewStatusInfo
  );
  public static Ingredient ingredient = Ingredient.of("ingredient1", 100, "g");
  public static RecipeIngredients recipeIngredients = RecipeIngredients.from(List.of(ingredient), recipeId);
  public static IngredientsInfo ingredientsInfo = IngredientsInfo.from(recipeIngredients);
  public static RecipeStep recipeStep = RecipeStep.from(1, "step1", List.of("description1"), 100.0, 200.0, recipeId);
  public static RecipeStepInfo recipeStepInfo = RecipeStepInfo.from(recipeStep);
  public static List<RecipeStepInfo> recipeStepInfos = List.of(recipeStepInfo);
  public static RecipeStatus recipeStatus = RecipeStatus.COMPLETED;
  public static FullRecipeInfo fullRecipeInfo = FullRecipeInfo.of(
      recipeStatus,
      videoInfo,
      ingredientsInfo,
      recipeStepInfos,
      recipeViewStatusInfo
  );
}