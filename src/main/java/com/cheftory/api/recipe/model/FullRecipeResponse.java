package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
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

    @JsonProperty("analysis")
    Analysis analysis,

    @JsonProperty("recipe_steps")
    List<Step> recipeSteps,

    @JsonProperty("view_status")
    ViewStatus viewStatus
) {

  public static FullRecipeResponse of(FullRecipeInfo fullRecipeInfo) {
    return new FullRecipeResponse(
        fullRecipeInfo.getRecipeStatus(),
        fullRecipeInfo.getVideoInfo() != null ? VideoInfo.from(fullRecipeInfo.getVideoInfo()) : null,
        fullRecipeInfo.getRecipeAnalysis() != null ? Analysis.from(fullRecipeInfo.getRecipeAnalysis()) : null,
        fullRecipeInfo.getRecipeStepInfos() != null ? fullRecipeInfo.getRecipeStepInfos().stream()
            .map(Step::from)
            .toList() : null,
        fullRecipeInfo.getRecipeViewStatus() != null ? ViewStatus.from(fullRecipeInfo.getRecipeViewStatus()) : null
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

  public record Analysis(
      @JsonProperty("id")
      UUID id,
      @JsonProperty("description")
      String description,

      @JsonProperty("ingredients")
      List<Ingredient> ingredients,

      @JsonProperty("tags")
      List<String> tags,

      @JsonProperty("servings")
      Integer servings,

      @JsonProperty("cook_time")
      Integer cookTime
  ) {

    public record Ingredient(
        @JsonProperty("name")
        String name,

        @JsonProperty("amount")
        Integer amount,

        @JsonProperty("unit")
        String unit
    ) {
      public static Ingredient from(RecipeAnalysis.Ingredient ingredient) {
        return new Ingredient(
            ingredient.getName(),
            ingredient.getAmount(),
            ingredient.getUnit()
        );
      }
    }

    public static Analysis from(RecipeAnalysis recipeAnalysis) {
      return new Analysis(
          recipeAnalysis.getId(),
          recipeAnalysis.getDescription(),
          recipeAnalysis.getIngredients().stream()
              .map(Ingredient::from)
              .toList(),
          recipeAnalysis.getTags(),
          recipeAnalysis.getServings(),
          recipeAnalysis.getCookTime()
      );
    }
  }
  private record Step(
      @JsonProperty("id")
      UUID id,

      @JsonProperty("step_order")
      Integer stepOrder,

      @JsonProperty("subtitle")
      String subtitle,

      @JsonProperty("details")
      List<Detail> details,

      @JsonProperty("start_time")
      Double startTime
  ) {
    private record Detail(
        @JsonProperty("text")
        String text,

        @JsonProperty("start")
        Double start
    ) {
      public static Detail from(RecipeStep.Detail detail) {
        return new Detail(
            detail.getText(),
            detail.getStart()
        );
      }
    }
    public static Step from(RecipeStep step) {
      return new Step(
          step.getId(),
          step.getStepOrder(),
          step.getSubtitle(),
          step.getDetails().stream()
                  .map(Detail::from).toList(),
          step.getStart()
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
    public static ViewStatus from(RecipeViewStatus viewStatus) {
      return new ViewStatus(
          viewStatus.getId(),
          viewStatus.getViewedAt(),
          viewStatus.getLastPlaySeconds(),
          viewStatus.getCreatedAt()
      );
    }
  }
}