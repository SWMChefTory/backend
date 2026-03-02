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
 * <p>진행 상태는 update가 아니라 이벤트(row) append 방식으로 저장되며, 같은 `recipeId`라도
 * 비동기 실행 단위(`jobId`)별로 별도 이력을 남깁니다.</p>
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
     * @param jobId 조회할 비동기 실행 식별자
     * @return 지정된 jobId 실행에 대한 진행 상태 목록 (생성 시각 오름차순)
     */
    public List<RecipeProgress> gets(UUID recipeId, UUID jobId) {
        return recipeProgressRepository.findAllByRecipeIdAndJobId(recipeId, jobId, RecipeProgressSort.CREATE_AT_ASC);
    }

    /**
     * 진행 상태를 시작으로 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param step 진행 단계
     * @param detail 상세 단계
     * @param jobId 비동기 실행 식별자
     */
    @DbThrottled
    public void start(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail, UUID jobId) {
        create(recipeId, step, detail, RecipeProgressState.RUNNING, jobId);
    }

    /**
     * 진행 상태를 성공으로 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param step 진행 단계
     * @param detail 상세 단계
     * @param jobId 비동기 실행 식별자
     */
    @DbThrottled
    public void success(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail, UUID jobId) {
        create(recipeId, step, detail, RecipeProgressState.SUCCESS, jobId);
    }

    /**
     * 진행 상태를 실패로 생성합니다.
     *
     * @param recipeId 레시피 ID
     * @param step 진행 단계
     * @param detail 상세 단계
     * @param jobId 비동기 실행 식별자
     */
    @DbThrottled
    public void failed(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail, UUID jobId) {
        create(recipeId, step, detail, RecipeProgressState.FAILED, jobId);
    }

    private void create(
            UUID recipeId,
            RecipeProgressStep step,
            RecipeProgressDetail detail,
            RecipeProgressState state,
            UUID jobId) {
        RecipeProgress recipeProgress = RecipeProgress.create(recipeId, jobId, clock, step, detail, state);
        recipeProgressRepository.save(recipeProgress);
    }
}
