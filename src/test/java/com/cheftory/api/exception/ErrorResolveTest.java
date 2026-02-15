package com.cheftory.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.user.exception.UserErrorCode;
import org.junit.jupiter.api.Test;

class ErrorResolveTest {

    @Test
    void shouldResolveByErrorCode() {
        assertThat(Error.resolveErrorCode(UserErrorCode.USER_NOT_FOUND.getErrorCode()))
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        assertThat(Error.resolveErrorCode(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND.getErrorCode()))
                .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
        assertThat(Error.resolveErrorCode(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode()))
                .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);
    }

    @Test
    void shouldFallbackToUnknownWhenNotFoundOrAmbiguous() {
        assertThat(Error.resolveErrorCode("NOT_EXISTING_ERROR")).isEqualTo(GlobalErrorCode.UNKNOWN_ERROR);
        assertThat(Error.resolveErrorCode("UNKNOWN_ERROR")).isEqualTo(GlobalErrorCode.UNKNOWN_ERROR);
    }
}
