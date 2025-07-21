package com.cheftory.api.recipeaccess;

import com.cheftory.api.recipeaccess.dto.AccessRecipeRequest;
import com.cheftory.api.recipeaccess.dto.FullRecipeResponse;
import com.cheftory.api.recipeaccess.dto.RecipeAccessResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes")
public class RecipeAccessController {
  private final RecipeAccessService recipeAccessService;

  @PostMapping("")
  public RecipeAccessResponse tryAccessRecipe(@RequestBody AccessRecipeRequest accessRecipeRequest, @RequestHeader("X-User-Id") UUID userId) {
    return recipeAccessService.tryAccessRecipe(accessRecipeRequest.getVideoUrl(),userId);
  }

  public FullRecipeResponse getFullRecipeResponse(UUID recipeId) {
    return recipeAccessService.accessFullRecipe(recipeId);
  }
}
