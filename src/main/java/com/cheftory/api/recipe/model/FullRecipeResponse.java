package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FullRecipeResponse(
    @JsonProperty("recipe_status")
    RecipeStatus recipeStatus,

    @JsonProperty("video_info")
    VideoInfo videoInfo,

    @JsonProperty("ingredients_info")
    Ingredients ingredientsInfo,

    @JsonProperty("recipe_steps")
    List<RecipeStep> recipeSteps,

    @JsonProperty("view_status")
    ViewStatus viewStatus
) {

  public static FullRecipeResponse of(FullRecipeInfo fullRecipeInfo) {
    return new FullRecipeResponse(
        fullRecipeInfo.getRecipeStatus(),
        fullRecipeInfo.getVideoInfo() != null ? VideoInfo.from(fullRecipeInfo.getVideoInfo()) : null,
        fullRecipeInfo.getIngredientsInfo() != null ? Ingredients.from(fullRecipeInfo.getIngredientsInfo()) : null,
        fullRecipeInfo.getRecipeStepInfos() != null ? fullRecipeInfo.getRecipeStepInfos().stream()
            .map(RecipeStep::from)
            .toList() : null,
        fullRecipeInfo.getRecipeViewStatusInfo() != null ? ViewStatus.from(fullRecipeInfo.getRecipeViewStatusInfo()) : null
    );
  }

  public record VideoInfo(
      @JsonProperty("video_id")
      String videoId,

      @JsonProperty("video_title")
      String title,

      @JsonProperty("video_thumbnail_url")
      URI thumbnailUrl,

      @JsonProperty("video_seconds")
      Integer videoSeconds
  ) {
    public static VideoInfo from(com.cheftory.api.recipe.entity.VideoInfo videoInfo) {
      return new VideoInfo(
          videoInfo.getVideoId(),
          videoInfo.getTitle(),
          videoInfo.getThumbnailUrl(),
          videoInfo.getVideoSeconds()
      );
    }
  }

  public record Ingredients(
      @JsonProperty("id")
      UUID id,

      @JsonProperty("ingredients")
      List<Ingredient> ingredients
  ) {
    public static Ingredients from(IngredientsInfo ingredientsInfo) {
      return new Ingredients(
          ingredientsInfo.getIngredientsId(),
          ingredientsInfo.getIngredients().stream()
              .map(Ingredient::from)
              .toList()
      );
    }
  }

  public record Ingredient(
      @JsonProperty("name")
      String name,

      @JsonProperty("amount")
      Integer amount,

      @JsonProperty("unit")
      String unit
  ) {
    public static Ingredient from(com.cheftory.api.recipe.ingredients.entity.Ingredient ingredient) {
      return new Ingredient(
          ingredient.getName(),
          ingredient.getAmount(),
          ingredient.getUnit()
      );
    }
  }

  public record RecipeStep(
      @JsonProperty("id")
      UUID id,

      @JsonProperty("step_order")
      Integer stepOrder,

      @JsonProperty("subtitle")
      String subtitle,

      @JsonProperty("details")
      List<String> details,

      @JsonProperty("start_time")
      Double startTime,

      @JsonProperty("end_time")
      Double endTime
  ) {
    public static RecipeStep from(RecipeStepInfo stepInfo) {
      return new RecipeStep(
          stepInfo.getId(),
          stepInfo.getStepOrder(),
          stepInfo.getSubtitle(),
          stepInfo.getDetails(),
          stepInfo.getStart(),
          stepInfo.getEnd()
      );
    }
  }

  public record ViewStatus(
      @JsonProperty("id")
      UUID id,

      @JsonProperty("viewed_at")
      LocalDateTime viewedAt,

      @JsonProperty("last_play_seconds")
      Integer lastPlaySeconds,

      @JsonProperty("created_at")
      LocalDateTime createdAt
  ) {
    public static ViewStatus from(RecipeViewStatusInfo viewStatusInfo) {
      return new ViewStatus(
          viewStatusInfo.getId(),
          viewStatusInfo.getViewedAt(),
          viewStatusInfo.getLastPlaySeconds(),
          viewStatusInfo.getCreatedAt()
      );
    }
  }
}