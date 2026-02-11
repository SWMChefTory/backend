package com.cheftory.api.recipe.report.repository;

import com.cheftory.api.recipe.report.entity.RecipeReport;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 신고 JPA 리포지토리
 */
public interface RecipeReportJpaRepository extends JpaRepository<RecipeReport, UUID> {}
