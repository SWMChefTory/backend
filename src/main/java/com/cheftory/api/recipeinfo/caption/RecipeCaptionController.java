package com.cheftory.api.recipeinfo.caption;

import com.cheftory.api.recipeinfo.caption.dto.RecipeCaptionsResponse;
import com.cheftory.api.recipeinfo.recipe.validator.ExistsRecipeId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/papi/v1/recipes")
@RequiredArgsConstructor
public class RecipeCaptionController {
  private final RecipeCaptionService recipeCaptionService;

  @GetMapping("/{recipeId}/caption")
  public RecipeCaptionsResponse getRecipeCaption(@PathVariable @ExistsRecipeId UUID recipeId) {
    return RecipeCaptionsResponse.from(recipeCaptionService.findByRecipeId(recipeId));
  }
}
