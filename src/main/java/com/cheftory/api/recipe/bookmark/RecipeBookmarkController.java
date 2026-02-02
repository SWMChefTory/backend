package com.cheftory.api.recipe.bookmark;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.bookmark.dto.RecipeBookmarkRequest;
import com.cheftory.api.recipe.content.info.validator.ExistsRecipeId;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/{recipeId}")
public class RecipeBookmarkController {
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeBookmarkFacade recipeBookmarkFacade;

    @PutMapping("/categories")
    public SuccessOnlyResponse updateBookmarkCategory(
            @RequestBody @Valid RecipeBookmarkRequest.UpdateCategory request,
            @PathVariable("recipeId") @ExistsRecipeId UUID recipeId,
            @UserPrincipal UUID userId) {
        recipeBookmarkService.updateCategory(userId, recipeId, request.categoryId());
        return SuccessOnlyResponse.create();
    }

    @DeleteMapping("/bookmark")
    public SuccessOnlyResponse deleteBookmark(
            @PathVariable("recipeId") @ExistsRecipeId UUID recipeId, @UserPrincipal UUID userId) {
        recipeBookmarkService.delete(userId, recipeId);
        return SuccessOnlyResponse.create();
    }

    @PostMapping("/bookmark")
    public SuccessOnlyResponse createBookmark(
            @PathVariable("recipeId") @ExistsRecipeId UUID recipeId, @UserPrincipal UUID userId) {
        recipeBookmarkFacade.createAndCharge(userId, recipeId);
        return SuccessOnlyResponse.create();
    }
}
