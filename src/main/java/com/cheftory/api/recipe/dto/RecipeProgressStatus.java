package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 생성 진행 상태 정보
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeProgressStatus {

    /**
     * 진행 상태 목록
     */
    private List<RecipeProgress> progresses;
    /**
     * 레시피 정보
     */
    private RecipeInfo recipe;

    /**
     * RecipeProgressStatus 생성 팩토리 메서드
     *
     * @param recipe 레시피 정보 엔티티
     * @param progresses 진행 상태 목록
     * @return 레시피 진행 상태 객체
     */
    public static RecipeProgressStatus of(RecipeInfo recipe, List<RecipeProgress> progresses) {
        return new RecipeProgressStatus(progresses, recipe);
    }
}
