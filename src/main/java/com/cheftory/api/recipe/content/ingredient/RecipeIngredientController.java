package com.cheftory.api.recipe.content.ingredient;

import com.cheftory.api.recipe.content.ingredient.dto.RecipeIngredientsResponse;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 재료 관련 API 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/papi/v1/recipes")
public class RecipeIngredientController {

    private final RecipeIngredientService recipeIngredientService;

    /**
     * 레시피 ID로 재료 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 재료 목록 응답 DTO
     */
    @GetMapping("/{recipeId}/ingredients")
    public RecipeIngredientsResponse getIngredients(@PathVariable UUID recipeId) {
        List<RecipeIngredient> recipeIngredients = recipeIngredientService.gets(recipeId);
        return RecipeIngredientsResponse.from(recipeIngredients);
    }
}
