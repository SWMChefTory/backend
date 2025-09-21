package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.briefing.RecipeBriefing;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredient;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.RecipeProgressStep;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FullRecipeResponse(
    @JsonProperty("recipe_status") RecipeStatus recipeStatus,
    @JsonProperty("video_info") VideoInfo videoInfo,
    @JsonProperty("recipe_ingredient") List<Ingredient> ingredients,
    @JsonProperty("recipe_progresses") List<Progress> recipeProgresses,
    @JsonProperty("recipe_steps") List<Step> recipeSteps,
    @JsonProperty("view_status") ViewStatus viewStatus,
    @JsonProperty("recipe_detail_meta") DetailMeta detailMeta,
    @JsonProperty("recipe_tags") List<Tag> tags,
    @JsonProperty("recipe_briefings") List<Briefing> briefings
) {

  public static FullRecipeResponse of(FullRecipeInfo fullRecipeInfo) {
    return new FullRecipeResponse(
        fullRecipeInfo.getRecipe().getRecipeStatus(),
        VideoInfo.from(fullRecipeInfo.getRecipeYoutubeMeta()),
        fullRecipeInfo.getRecipeIngredients().stream().map(Ingredient::from).toList(),
        fullRecipeInfo.getRecipeProgresses().stream().map(Progress::from).toList(),
        fullRecipeInfo.getRecipeSteps().stream().map(Step::from).toList(),
        ViewStatus.from(fullRecipeInfo.getRecipeViewStatus()),
        fullRecipeInfo.getRecipeDetailMeta() != null
            ? DetailMeta.from(fullRecipeInfo.getRecipeDetailMeta())
            : null,
        fullRecipeInfo.getRecipeTags().stream().map(Tag::from).toList(),
        fullRecipeInfo.getRecipeBriefings().stream().map(Briefing::from).toList()
    );
  }

  private record DetailMeta(
      @JsonProperty("description") String description,
      @JsonProperty("servings") Integer servings,
      @JsonProperty("cookingTime") Integer cookingTime) {

    public static DetailMeta from(RecipeDetailMeta detailMeta) {
      return new DetailMeta(
          detailMeta.getDescription(), detailMeta.getServings(), detailMeta.getCookTime());
    }
  }

  private record VideoInfo(
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_title") String title,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_seconds") Integer videoSeconds) {
    public static VideoInfo from(RecipeYoutubeMeta youtubeMeta) {
      return new VideoInfo(
          youtubeMeta.getVideoId(),
          youtubeMeta.getTitle(),
          youtubeMeta.getThumbnailUrl(),
          youtubeMeta.getVideoSeconds());
    }
  }

  private record Ingredient(
      @JsonProperty("name") String name,
      @JsonProperty("amount") Integer amount,
      @JsonProperty("unit") String unit) {
    public static Ingredient from(RecipeIngredient ingredient) {
      return new Ingredient(ingredient.getName(), ingredient.getAmount(), ingredient.getUnit());
    }
  }

  private record Briefing(
      @JsonProperty("content") String content) {
    public static Briefing from(RecipeBriefing briefing) {
      return new Briefing(briefing.getContent());
    }
  }

  private record Step(
      @JsonProperty("id") UUID id,
      @JsonProperty("step_order") Integer stepOrder,
      @JsonProperty("subtitle") String subtitle,
      @JsonProperty("details") List<Detail> details,
      @JsonProperty("start_time") Double startTime) {
    private record Detail(@JsonProperty("text") String text, @JsonProperty("start") Double start) {

      public static Detail from(RecipeStep.Detail detail) {
        return new Detail(detail.getText(), detail.getStart());
      }
    }

    public static Step from(RecipeStep step) {
      return new Step(
          step.getId(),
          step.getStepOrder(),
          step.getSubtitle(),
          step.getDetails().stream().map(Detail::from).toList(),
          step.getStart());
    }
  }

  private record ViewStatus(
      @JsonProperty("id") UUID id,
      @JsonProperty("viewed_at") LocalDateTime viewedAt,
      @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
      @JsonProperty("created_at") LocalDateTime createdAt) {
    public static ViewStatus from(RecipeViewStatus viewStatus) {
      return new ViewStatus(
          viewStatus.getId(),
          viewStatus.getViewedAt(),
          viewStatus.getLastPlaySeconds(),
          viewStatus.getCreatedAt());
    }
  }

  private record Progress(
      @JsonProperty("step") RecipeProgressStep step,
      @JsonProperty("detail") RecipeProgressDetail detail) {
    public static Progress from(RecipeProgress progress) {
      return new Progress(progress.getStep(), progress.getDetail());
    }
  }

  private record Tag(@JsonProperty("name") String name) {
    public static Tag from(RecipeTag tag) {
      return new Tag(tag.getTag());
    }
  }
}
