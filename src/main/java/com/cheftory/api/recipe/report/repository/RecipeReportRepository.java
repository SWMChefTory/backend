package com.cheftory.api.recipe.report.repository;

import com.cheftory.api.recipe.report.entity.RecipeReport;
import com.cheftory.api.recipe.report.exception.RecipeReportException;

/**
 * 레시피 신고 데이터 접근 인터페이스
 */
public interface RecipeReportRepository {

    /**
     * 레시피 신고 저장
     *
     * @param report 저장할 신고 엔티티
     * @throws RecipeReportException 중복 신고일 때
     */
    void create(RecipeReport report) throws RecipeReportException;
}
