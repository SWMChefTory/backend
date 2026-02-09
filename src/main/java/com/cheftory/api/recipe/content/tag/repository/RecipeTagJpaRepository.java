package com.cheftory.api.recipe.content.tag.repository;

import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 태그 JPA Repository
 */
public interface RecipeTagJpaRepository extends JpaRepository<RecipeTag, UUID> {
    /**
     * 레시피 ID로 태그 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 태그 목록
     */
    List<RecipeTag> findAllByRecipeId(UUID recipeId);

    /**
     * 여러 레시피 ID로 태그 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 태그 목록
     */
    List<RecipeTag> findAllByRecipeIdIn(List<UUID> recipeIds);
}
