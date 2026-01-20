package com.cheftory.api.recipe.rank;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class RecipeRankController {
    private final RecipeRankService recipeRankService;

    @PutMapping("/papi/v1/recipes/trending/{recipeIds}")
    public SuccessOnlyResponse updateTrendingRecipes(@PathVariable List<UUID> recipeIds) {
        recipeRankService.updateRecipes(RankingType.TRENDING, recipeIds);
        return SuccessOnlyResponse.create();
    }

    @PutMapping("/papi/v1/recipes/chef/{recipeIds}")
    public SuccessOnlyResponse updateChefRecipes(@PathVariable List<UUID> recipeIds) {
        recipeRankService.updateRecipes(RankingType.CHEF, recipeIds);
        return SuccessOnlyResponse.create();
    }
}
