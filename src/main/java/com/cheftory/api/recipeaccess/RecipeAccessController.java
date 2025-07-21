package com.cheftory.api.recipeaccess;

import com.cheftory.api.recipeaccess.dto.AccessRecipeRequest;
import com.cheftory.api.recipeaccess.dto.FullRecipeResponse;
import com.cheftory.api.recipeaccess.dto.RecipeAccessResponse;
import com.cheftory.api.recipeaccess.dto.SimpleAccessInfosResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes")
public class RecipeAccessController {
  private final RecipeAccessService recipeAccessService;

  @PostMapping("")
  public RecipeAccessResponse tryAccessRecipe(@RequestBody AccessRecipeRequest accessRecipeRequest, @AuthenticationPrincipal UUID userId) {
    return recipeAccessService.tryAccessRecipe(accessRecipeRequest.getVideoUrl(),userId);
  }

  @GetMapping("/{recipeId}")
  public FullRecipeResponse getFullRecipeResponse(@PathVariable("recipeId") UUID recipeId) {
    return recipeAccessService.accessFullRecipe(recipeId);
  }

  @GetMapping("")
  public SimpleAccessInfosResponse getSimpleAccessInfosResponse(@AuthenticationPrincipal UUID userId) {
    return recipeAccessService.accessByRecentOrder(userId);
  }
}
