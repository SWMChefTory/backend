package com.cheftory.api.recipe.viewstatus;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/{recipe_id}")
public class RecipeViewStatusController {
  private final RecipeViewStatusService recipeViewStatusService;

  @PutMapping("/categories")
  public SuccessOnlyResponse updateViewStatusCategory(
      @RequestBody @Valid RecipeViewStatusRequest.UpdateCategory request,
      @PathVariable("recipe_id") UUID recipeId,
      @UserPrincipal UUID userId
  ) {
    recipeViewStatusService.updateCategory(userId, recipeId, request.categoryId());
    return SuccessOnlyResponse.create();
  }
}
