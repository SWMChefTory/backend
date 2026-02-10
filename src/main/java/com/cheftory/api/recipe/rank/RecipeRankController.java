package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 랭킹 관련 API 요청을 처리하는 컨트롤러.
 *
 * <p>트렌딩, 셰프 랭킹 레시피를 업데이트하는 기능을 제공합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class RecipeRankController {
    private final RecipeRankService recipeRankService;

    /**
     * 트렌딩 랭킹 레시피를 업데이트합니다.
     *
     * @param recipeIds 레시피 ID 목록
     * @return 성공 응답
     */
    @PutMapping("/papi/v1/recipes/trending/{recipeIds}")
    public SuccessOnlyResponse updateTrendingRecipes(@PathVariable List<UUID> recipeIds) {
        recipeRankService.updateRecipes(RankingType.TRENDING, recipeIds);
        return SuccessOnlyResponse.create();
    }

    /**
     * 셰프 랭킹 레시피를 업데이트합니다.
     *
     * @param recipeIds 레시피 ID 목록
     * @return 성공 응답
     */
    @PutMapping("/papi/v1/recipes/chef/{recipeIds}")
    public SuccessOnlyResponse updateChefRecipes(@PathVariable List<UUID> recipeIds) {
        recipeRankService.updateRecipes(RankingType.CHEF, recipeIds);
        return SuccessOnlyResponse.create();
    }
}
