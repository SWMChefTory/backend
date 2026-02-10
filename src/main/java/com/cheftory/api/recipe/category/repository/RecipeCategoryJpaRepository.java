package com.cheftory.api.recipe.category.repository;

import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.entity.RecipeCategoryStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 카테고리 JPA Repository
 */
public interface RecipeCategoryJpaRepository extends JpaRepository<RecipeCategory, UUID> {

    List<RecipeCategory> findAllByUserIdAndStatus(UUID userId, RecipeCategoryStatus status);

    Optional<RecipeCategory> findByIdAndUserIdAndStatus(UUID id, UUID userId, RecipeCategoryStatus status);

    boolean existsByIdAndStatus(UUID id, RecipeCategoryStatus status);
}
