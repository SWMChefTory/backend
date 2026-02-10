package com.cheftory.api.recipe.creation.progress;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressState;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.creation.progress.utils.RecipeProgressSort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 생성 진행 상태 서비스.
 *
 * <p>레시피 생성 파이프라인의 각 단계 진행 상태를 관리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeProgressService {
    private final RecipeProgressRepository recipeProgressRepository;
    private final Clock clock;

    /**
     * 레시피 ID로 진행 상태 목록을 조회합니다.
     *
     * @param recipeId 레시피 ID
     * @return 진행 상태 목록
     */
    public List<RecipeProgress> gets(UUID recipeId) {
        return recipeProgressRepository.findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
    }

    /**
     * 진행 상태를 시작으로 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param step 진행 단계
     * @param detail 상세 단계
     */
    @DbThrottled
    public void start(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
        create(recipeId, step, detail, RecipeProgressState.RUNNING);
    }

    /**
     * 진행 상태를 성공으로 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param step 진행 단계
     * @param detail 상세 단계
     */
    @DbThrottled
    public void success(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
        create(recipeId, step, detail, RecipeProgressState.SUCCESS);
    }

    /**
     * 진행 상태를 실패로 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param step 진행 단계
     * @param detail 상세 단계
     */
    @DbThrottled
    public void failed(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
        create(recipeId, step, detail, RecipeProgressState.FAILED);
    }

    private void create(
            UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail, RecipeProgressState state) {
        RecipeProgress recipeProgress = RecipeProgress.create(recipeId, clock, step, detail, state);
        recipeProgressRepository.save(recipeProgress);
    }
}
