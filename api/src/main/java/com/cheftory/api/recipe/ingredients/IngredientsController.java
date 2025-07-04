package com.cheftory.api.recipe.ingredients;

import com.cheftory.api.recipe.ingredients.dto.IngredientsFindResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recipeinfos")
public class IngredientsController {
    public final FindRecipeIngredientsService findRecipeIngredientsService;

    @GetMapping("/{recipeInfoId}/ingredients")
    public IngredientsFindResponse findIngredients(@PathVariable UUID recipeInfoId) {
        return findRecipeIngredientsService.findIngredients(recipeInfoId);
    }
}
