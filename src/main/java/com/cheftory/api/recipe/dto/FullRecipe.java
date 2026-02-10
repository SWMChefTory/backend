package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 전체 레시피 정보 집계
 *
 * <p>레시피의 모든 관련 엔티티를 포함하는 집계 객체입니다.</p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipe {
    /**
     * 재료 목록
     */
    private List<RecipeIngredient> recipeIngredients;
    /**
     * 태그 목록
     */
    private List<RecipeTag> recipeTags;

    /**
     * 상세 메타데이터 (nullable)
     */
    @Nullable
    private RecipeDetailMeta recipeDetailMeta;

    /**
     * 조리 단계 목록
     */
    private List<RecipeStep> recipeSteps;
    /**
     * 진행 상태 목록
     */
    private List<RecipeProgress> recipeProgresses;

    /**
     * 레시피 북마크 (nullable)
     */
    @Nullable
    private RecipeBookmark recipeBookmark;

    /**
     * YouTube 메타데이터
     */
    private RecipeYoutubeMeta recipeYoutubeMeta;
    /**
     * 레시피 정보
     */
    private RecipeInfo recipe;
    /**
     * 브리핑 목록
     */
    private List<RecipeBriefing> recipeBriefings;

    /**
     * 소유한 레시피의 FullRecipe 생성
     *
     * @param recipeSteps 조리 단계 목록
     * @param recipeIngredients 재료 목록
     * @param recipeDetailMeta 상세 메타데이터
     * @param recipeProgresses 진행 상태 목록
     * @param recipeTags 태그 목록
     * @param recipeYoutubeMeta YouTube 메타데이터
     * @param recipeBookmark 레시피 북마크
     * @param recipe 레시피 정보
     * @param recipeBriefings 브리핑 목록
     * @return 전체 레시피 객체
     */
    public static FullRecipe owned(
            List<RecipeStep> recipeSteps,
            List<RecipeIngredient> recipeIngredients,
            @Nullable RecipeDetailMeta recipeDetailMeta,
            List<RecipeProgress> recipeProgresses,
            List<RecipeTag> recipeTags,
            RecipeYoutubeMeta recipeYoutubeMeta,
            @Nullable RecipeBookmark recipeBookmark,
            RecipeInfo recipe,
            List<RecipeBriefing> recipeBriefings) {

        return new FullRecipe(
                recipeIngredients,
                recipeTags,
                recipeDetailMeta,
                recipeSteps,
                recipeProgresses,
                recipeBookmark,
                recipeYoutubeMeta,
                recipe,
                recipeBriefings);
    }

    /**
     * 소유하지 않은 레시피의 FullRecipe 생성
     *
     * @param recipeSteps 조리 단계 목록
     * @param recipeIngredients 재료 목록
     * @param recipeDetailMeta 상세 메타데이터
     * @param recipeProgresses 진행 상태 목록
     * @param recipeTags 태그 목록
     * @param recipeYoutubeMeta YouTube 메타데이터
     * @param recipe 레시피 정보
     * @param recipeBriefings 브리핑 목록
     * @return 전체 레시피 객체
     */
    public static FullRecipe notOwned(
            List<RecipeStep> recipeSteps,
            List<RecipeIngredient> recipeIngredients,
            @Nullable RecipeDetailMeta recipeDetailMeta,
            List<RecipeProgress> recipeProgresses,
            List<RecipeTag> recipeTags,
            RecipeYoutubeMeta recipeYoutubeMeta,
            RecipeInfo recipe,
            List<RecipeBriefing> recipeBriefings) {

        return new FullRecipe(
                recipeIngredients,
                recipeTags,
                recipeDetailMeta,
                recipeSteps,
                recipeProgresses,
                null,
                recipeYoutubeMeta,
                recipe,
                recipeBriefings);
    }
}
