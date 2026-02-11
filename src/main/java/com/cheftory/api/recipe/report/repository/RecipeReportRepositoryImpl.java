package com.cheftory.api.recipe.report.repository;

import com.cheftory.api.recipe.report.entity.RecipeReport;
import com.cheftory.api.recipe.report.exception.RecipeReportErrorCode;
import com.cheftory.api.recipe.report.exception.RecipeReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

/**
 * 레시피 신고 데이터 접근 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecipeReportRepositoryImpl implements RecipeReportRepository {

    private final RecipeReportJpaRepository jpaRepository;

    @Override
    public void create(RecipeReport report) throws RecipeReportException {
        try {
            jpaRepository.save(report);
        } catch (DataIntegrityViolationException e) {
            throw new RecipeReportException(RecipeReportErrorCode.DUPLICATE_REPORT);
        }
    }
}
