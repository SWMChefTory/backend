package com.cheftory.api.recipe.category;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import com.cheftory.api.recipe.category.repository.RecipeCategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 카테고리 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RecipeCategoryService {

    private final RecipeCategoryRepository recipeCategoryRepository;
    private final Clock clock;

    /**
     * 레시피 카테고리 생성
     *
     * @param name 카테고리 이름
     * @param userId 유저 ID
     * @return 생성된 카테고리 ID
     * @throws RecipeCategoryException 이름이 비어있을 때 RECIPE_CATEGORY_NAME_EMPTY
     */
    public UUID create(String name, UUID userId) throws RecipeCategoryException {
        RecipeCategory recipeCategory = RecipeCategory.create(clock, name, userId);
        return recipeCategoryRepository.create(recipeCategory);
    }

    /**
     * 레시피 카테고리 삭제
     *
     * @param userId 유저 ID
     * @param recipeCategoryId 카테고리 ID
     * @throws RecipeCategoryException 카테고리를 찾을 수 없을 때 RECIPE_CATEGORY_NOT_FOUND
     */
    public void delete(UUID userId, UUID recipeCategoryId) throws RecipeCategoryException {
        recipeCategoryRepository.delete(userId, recipeCategoryId);
    }

    /**
     * 유저의 레시피 카테고리 목록 조회
     *
     * @param userId 유저 ID
     * @return 레시피 카테고리 목록
     */
    public List<RecipeCategory> getUsers(UUID userId) {
        return recipeCategoryRepository.gets(userId);
    }

    /**
     * 레시피 카테고리 존재 여부 확인
     *
     * @param recipeCategoryId 카테고리 ID
     * @return 존재 여부
     */
    public boolean exists(UUID recipeCategoryId) {
        return recipeCategoryRepository.exists(recipeCategoryId);
    }
}
