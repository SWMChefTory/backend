package com.cheftory.api.recipe.ingredients;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recipeinfo/{recipeInfoId}/ingredients/{ingredientsId}")
public class IngredientsController {
    public final FindRecipeIngredientsService findRecipeIngredientsService;

}
