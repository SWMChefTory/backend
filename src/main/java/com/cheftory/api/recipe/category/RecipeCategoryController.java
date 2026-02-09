package com.cheftory.api.recipe.category;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.category.dto.RecipeCategoryRequest;
import com.cheftory.api.recipe.category.dto.RecipeCategoryResponse;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 카테고리 관련 API 요청을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/categories")
public class RecipeCategoryController {
    private final RecipeCategoryService service;

    /**
     * 레시피 카테고리 생성
     *
     * @param request 카테고리 생성 요청 정보 (이름)
     * @param userId 인증된 유저 ID (@UserPrincipal로 주입됨)
     * @return 생성된 카테고리 ID 응답 DTO
     * @throws RecipeCategoryException 이름이 비어있을 때 RECIPE_CATEGORY_NAME_EMPTY
     */
    @PostMapping
    public RecipeCategoryResponse.Create createCategory(
            @RequestBody RecipeCategoryRequest.Create request, @UserPrincipal UUID userId)
            throws RecipeCategoryException {
        UUID recipeCategoryId = service.create(request.name(), userId);
        return RecipeCategoryResponse.Create.from(recipeCategoryId);
    }
}
