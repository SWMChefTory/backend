package com.cheftory.api.recipe;

import com.cheftory.api.recipe.dto.PreCreationRecipeResponse;
import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recipeinfos")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    @PostMapping("")
    public PreCreationRecipeResponse createRecipe(@RequestBody RecipeCreateRequest recipeCreateRequest) {
        return recipeService.CreateRecipe(recipeCreateRequest);
    }
}
