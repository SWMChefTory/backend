package com.cheftory.api.recipe.report;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.content.info.validator.ExistsRecipeId;
import com.cheftory.api.recipe.report.dto.RecipeReportRequest;
import com.cheftory.api.recipe.report.exception.RecipeReportException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 신고 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/{recipeId}")
public class RecipeReportController {

    private final RecipeReportService service;

    /**
     * 레시피 신고 생성
     *
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @param request 신고 요청
     * @return 성공 응답
     * @throws RecipeReportException 중복 신고일 때
     */
    @PostMapping("/reports")
    public SuccessOnlyResponse report(
            @PathVariable @ExistsRecipeId UUID recipeId,
            @UserPrincipal UUID userId,
            @RequestBody @Valid RecipeReportRequest request)
            throws RecipeReportException {
        service.report(userId, recipeId, request.reason(), request.description());
        return SuccessOnlyResponse.create();
    }
}
