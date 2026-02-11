package com.cheftory.api.recipe.report.exception;

import com.cheftory.api.exception.CheftoryException;

/**
 * 레시피 신고 관련 예외
 */
public class RecipeReportException extends CheftoryException {

    public RecipeReportException(RecipeReportErrorCode errorCode) {
        super(errorCode);
    }
}
