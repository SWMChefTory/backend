package com.cheftory.api.recipe.content.step;

import com.cheftory.api.recipe.content.info.validator.ExistsRecipeId;
import com.cheftory.api.recipe.content.step.dto.RecipeStepsResponse;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 단계 관련 API 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/papi/v1/recipes")
@RequiredArgsConstructor
public class RecipeStepController {

    private final RecipeStepService recipeStepService;

    /**
     * 레시피 ID로 단계 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 단계 목록 응답 DTO
     */
    @GetMapping("/{recipeId}/steps")
    public RecipeStepsResponse getRecipeSteps(@PathVariable @ExistsRecipeId UUID recipeId) {
        List<RecipeStep> recipesStep = recipeStepService.gets(recipeId);
        return RecipeStepsResponse.from(recipesStep);
    }
}
