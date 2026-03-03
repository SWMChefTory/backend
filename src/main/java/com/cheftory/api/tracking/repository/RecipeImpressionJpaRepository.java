package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeImpression;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 노출 JPA 리포지토리.
 */
public interface RecipeImpressionJpaRepository extends JpaRepository<RecipeImpression, UUID> {}
