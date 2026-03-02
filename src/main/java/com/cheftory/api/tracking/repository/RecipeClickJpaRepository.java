package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeClick;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 클릭 JPA 리포지토리.
 */
public interface RecipeClickJpaRepository extends JpaRepository<RecipeClick, UUID> {}
