package com.cheftory.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("ErrorStatusResolver")
class ErrorStatusResolverTest {

    private final ErrorStatusResolver resolver = new ErrorStatusResolver();

    @Test
    @DisplayName("NOT_FOUND 타입은 404를 반환한다")
    void shouldReturn404ForNotFound() {
        HttpStatus status = resolver.resolve(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("CONFLICT 타입은 409를 반환한다")
    void shouldReturn409ForConflict() {
        HttpStatus status = resolver.resolve(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_ALREADY_EXISTS);
        assertThat(status).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("UNAUTHORIZED 타입은 401을 반환한다")
    void shouldReturn401ForUnauthorized() {
        HttpStatus status = resolver.resolve(AuthErrorCode.INVALID_TOKEN);
        assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("INTERNAL 타입은 500을 반환한다")
    void shouldReturn500ForInternal() {
        HttpStatus status = resolver.resolve(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_CREATE_FAIL);
        assertThat(status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
