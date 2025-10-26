package com.cheftory.api.recipeinfo.history;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipeinfo.recipe.validator.ExistsRecipeId;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/{recipeId}")
public class RecipeHistoryController {
  private final RecipeHistoryService recipeHistoryService;

  @PutMapping("/categories")
  public SuccessOnlyResponse updateHistoryCategory(
      @RequestBody @Valid RecipeHistoryRequest.UpdateCategory request,
      @PathVariable("recipeId") @ExistsRecipeId UUID recipeId,
      @UserPrincipal UUID userId) {
    recipeHistoryService.updateCategory(userId, recipeId, request.categoryId());
    return SuccessOnlyResponse.create();
  }

  @DeleteMapping("")
  public SuccessOnlyResponse deleteHistory(
      @PathVariable("recipeId") @ExistsRecipeId UUID recipeId, @UserPrincipal UUID userId) {
    recipeHistoryService.delete(userId, recipeId);
    return SuccessOnlyResponse.create();
  }
}
