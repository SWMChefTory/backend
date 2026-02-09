package com.cheftory.api.recipe.content.step;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.step.client.RecipeStepClient;
import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.step.entity.RecipeStepSort;
import com.cheftory.api.recipe.content.step.exception.RecipeStepException;
import com.cheftory.api.recipe.content.step.repository.RecipeStepRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 단계 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RecipeStepService {
    private final RecipeStepClient recipeStepClient;
    private final RecipeStepRepository recipeStepRepository;
    private final Clock clock;

    /**
     * 레시피 단계 생성
     *
     * <p>외부 클라이언트를 통해 파일 URI로부터 레시피 단계 정보를 추출하여 저장합니다.</p>
     *
     * @param recipeId 레시피 ID
     * @param fileUri 파일 URI
     * @param mimeType 파일 MIME 타입
     * @return 생성된 레시피 단계 ID 목록
     * @throws RecipeStepException 단계 생성 중 예외 발생 시
     */
    public List<UUID> create(UUID recipeId, String fileUri, String mimeType) throws RecipeStepException {
        ClientRecipeStepsResponse response = recipeStepClient.fetch(fileUri, mimeType);

        List<RecipeStep> recipeSteps = response.toRecipeSteps(recipeId, clock);

        return recipeStepRepository.create(recipeSteps);
    }

    /**
     * 레시피 ID로 단계 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 정렬된 레시피 단계 목록
     */
    public List<RecipeStep> gets(UUID recipeId) {
        return recipeStepRepository.finds(recipeId, RecipeStepSort.STEP_ORDER_ASC);
    }
}
