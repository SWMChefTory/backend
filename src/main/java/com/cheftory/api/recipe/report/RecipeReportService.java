package com.cheftory.api.recipe.report;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.report.entity.RecipeReport;
import com.cheftory.api.recipe.report.entity.RecipeReportReason;
import com.cheftory.api.recipe.report.exception.RecipeReportException;
import com.cheftory.api.recipe.report.repository.RecipeReportRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 신고 서비스
 */
@Service
@RequiredArgsConstructor
public class RecipeReportService {

    private final RecipeReportRepository repository;
    private final Clock clock;

    /**
     * 레시피 신고 생성
     *
     * @param reporterId 신고자 ID
     * @param recipeId 레시피 ID
     * @param reason 신고 사유
     * @param description 상세 설명
     * @throws RecipeReportException 중복 신고일 때
     */
    public void report(UUID reporterId, UUID recipeId, RecipeReportReason reason, String description)
            throws RecipeReportException {
        RecipeReport report = RecipeReport.create(clock, reporterId, recipeId, reason, description);
        repository.create(report);
    }
}
