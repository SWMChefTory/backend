package com.cheftory.api.recipe.category;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.category.dto.RecipeCategoryRequest;
import com.cheftory.api.recipe.category.dto.RecipeCategoryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/categories")
public class RecipeCategoryController {
  private final RecipeCategoryService recipeCategoryService;

  @PostMapping
  public RecipeCategoryResponse.Create createCategory(
      @RequestBody RecipeCategoryRequest.Create request, @UserPrincipal UUID userId) {
    UUID recipeCategoryId = recipeCategoryService.create(request.name(), userId);
    return RecipeCategoryResponse.Create.from(recipeCategoryId);
  }
}
