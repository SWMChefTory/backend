package com.cheftory.api.recipe;

import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.dto.RecipeCreateResponse;
import com.cheftory.api.recipe.dto.RecipeFindResponse;
import com.cheftory.api.recipe.dto.RecipeOverviewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping("")
    public RecipeCreateResponse createRecipe(@RequestBody RecipeCreateRequest recipeCreateRequest) {
        return recipeService.checkRecipeAndCreate(recipeCreateRequest);
    }

    @GetMapping("/{recipeId}")
    public RecipeFindResponse getRecipe(@PathVariable UUID recipeId) {
        return recipeService.findTotalRecipeInfo(recipeId);
    }

    @GetMapping("")
    public RecipeOverviewsResponse getRecipeOverviewsResponse(UUID recipeId) {
        return recipeService
                .findRecipeOverviewsResponse();
    }
}
