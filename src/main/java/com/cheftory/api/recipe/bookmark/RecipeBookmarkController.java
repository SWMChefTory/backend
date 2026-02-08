package com.cheftory.api.recipe.bookmark;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.bookmark.dto.RecipeBookmarkRequest;
import com.cheftory.api.recipe.content.info.validator.ExistsRecipeId;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 레시피 북마크 관련 API 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/{recipeId}")
public class RecipeBookmarkController {
    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeBookmarkFacade recipeBookmarkFacade;

    /**
     * 레시피 북마크 카테고리 수정
     *
     * @param request 카테고리 수정 요청 (categoryId 필수)
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @return 성공 응답
     */
    @PutMapping("/categories")
    public SuccessOnlyResponse updateCategory(
            @RequestBody @Valid RecipeBookmarkRequest.UpdateCategory request,
            @PathVariable("recipeId") @ExistsRecipeId UUID recipeId,
            @UserPrincipal UUID userId) {
        recipeBookmarkService.categorize(userId, recipeId, request.categoryId());
        return SuccessOnlyResponse.create();
    }

    /**
     * 레시피 북마크 삭제
     *
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @return 성공 응답
     */
    @DeleteMapping("/bookmark")
    public SuccessOnlyResponse delete(
            @PathVariable("recipeId") @ExistsRecipeId UUID recipeId, @UserPrincipal UUID userId) {
        recipeBookmarkService.delete(userId, recipeId);
        return SuccessOnlyResponse.create();
    }

    /**
     * 레시피 북마크 생성 (크레딧 차감 포함)
     *
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @return 성공 응답
     */
    @PostMapping("/bookmark")
    public SuccessOnlyResponse create(
            @PathVariable("recipeId") @ExistsRecipeId UUID recipeId, @UserPrincipal UUID userId) {
        recipeBookmarkFacade.create(userId, recipeId);
        return SuccessOnlyResponse.create();
    }
}
