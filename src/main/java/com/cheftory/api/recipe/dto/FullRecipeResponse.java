package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipeResponse {

  @JsonProperty("recipe_status")
  private RecipeStatus recipeStatus;
  @JsonProperty("video_info")
  private VideoInfo videoInfo;
  @JsonProperty("ingredients_info")
  private Ingredients ingredientsInfo;
  @JsonProperty("recipe_steps")
  private List<RecipeStep> recipeSteps;
  @JsonProperty("view_status")
  private ViewStatus viewStatus;

  public static FullRecipeResponse of(FullRecipeInfo fullRecipeInfo) {
    return FullRecipeResponse.builder()
        .recipeStatus(fullRecipeInfo.getRecipeStatus())
        .videoInfo(VideoInfo.from(fullRecipeInfo.getVideoInfo()))
        .ingredientsInfo(Ingredients.from(fullRecipeInfo.getIngredientsInfo()))
        .recipeSteps(convertRecipeSteps(fullRecipeInfo.getRecipeStepInfos()))
        .viewStatus(ViewStatus.from(fullRecipeInfo.getRecipeViewStatusInfo()))
        .build();
  }

  private static List<RecipeStep> convertRecipeSteps(List<RecipeStepInfo> stepInfos) {
    return stepInfos.stream()
            .map(RecipeStep::from)
            .toList();
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder(access = AccessLevel.PRIVATE)
  @Getter
  public static class VideoInfo {
    @JsonProperty("video_id")
    private String videoId;
    @JsonProperty("video_title")
    private String title;
    @JsonProperty("video_thumbnail_url")
    private URI thumbnailUrl;
    @JsonProperty("video_seconds")
    private Integer videoSeconds;

    public static VideoInfo from(com.cheftory.api.recipe.entity.VideoInfo videoInfo) {
      return VideoInfo.builder()
          .videoId(videoInfo.getVideoId())
          .title(videoInfo.getTitle())
          .thumbnailUrl(videoInfo.getThumbnailUrl())
          .videoSeconds(videoInfo.getVideoSeconds())
          .build();
    }
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder(access = AccessLevel.PRIVATE)
  @Getter
  public static class Ingredients {
    @JsonProperty("ingredients_id")
    private UUID ingredientsId;
    @JsonProperty("ingredients")
    private List<Ingredient> ingredients;

    public static Ingredients from(IngredientsInfo ingredientsInfo) {
      return Ingredients.builder()
              .ingredientsId(ingredientsInfo.getIngredientsId())
              .ingredients(ingredientsInfo.getIngredients().stream()
                  .map(Ingredient::from)
                  .toList())
              .build();
    }
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder(access = AccessLevel.PRIVATE)
  @Getter
  public static class Ingredient {
    @JsonProperty("name")
    private String name;
    @JsonProperty("amount")
    private Integer amount;
    @JsonProperty("unit")
    private String unit;

    public static Ingredient from(com.cheftory.api.recipe.ingredients.entity.Ingredient ingredient) {
      return Ingredient.builder()
          .name(ingredient.getName())
          .amount(ingredient.getAmount())
          .unit(ingredient.getUnit())
          .build();
    }
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder(access = AccessLevel.PRIVATE)
  @Getter
  public static class RecipeStep {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("step_order")
    private Integer stepOrder;
    @JsonProperty("subtitle")
    private String subtitle;
    @JsonProperty("details")
    private List<String> details;
    @JsonProperty("start_time")
    private Double startTime;
    @JsonProperty("end_time")
    private Double endTime;

    public static RecipeStep from(RecipeStepInfo stepInfo) {
      return RecipeStep.builder()
          .id(stepInfo.getId())
          .stepOrder(stepInfo.getStepOrder())
          .subtitle(stepInfo.getSubtitle())
          .details(stepInfo.getDetails())
          .startTime(stepInfo.getStart())
          .endTime(stepInfo.getEnd())
          .build();
    }
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder(access = AccessLevel.PRIVATE)
  @Getter
  public static class ViewStatus {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("viewed_at")
    private LocalDateTime viewedAt;
    @JsonProperty("last_play_seconds")
    private Integer lastPlaySeconds;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ViewStatus from(RecipeViewStatusInfo viewStatusInfo) {
      return ViewStatus.builder()
              .id(viewStatusInfo.getId())
              .viewedAt(viewStatusInfo.getViewedAt())
              .lastPlaySeconds(viewStatusInfo.getLastPlaySeconds())
              .createdAt(viewStatusInfo.getCreatedAt())
              .build();
    }
  }
}