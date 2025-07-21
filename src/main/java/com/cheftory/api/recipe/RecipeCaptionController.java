package com.cheftory.api.recipe;

import com.cheftory.api.recipe.caption.RecipeCaptionService;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/papi/v1/recipes")
@RequiredArgsConstructor
public class RecipeCaptionController {
    private final RecipeCaptionService recipeCaptionService;

    @GetMapping("/{recipeId}/caption")
    public CaptionInfo getRecipeCaption(@PathVariable UUID recipeId) {
        return recipeCaptionService
                .findCaptionInfo(recipeId);
    }

}
