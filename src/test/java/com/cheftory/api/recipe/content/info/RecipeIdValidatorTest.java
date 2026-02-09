package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.info.validator.RecipeIdValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeIdValidator 테스트")
class RecipeIdValidatorTest {

    private RecipeInfoService recipeInfoService;
    private RecipeIdValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        recipeInfoService = mock(RecipeInfoService.class);
        validator = new RecipeIdValidator(recipeInfoService);
        context = mock(ConstraintValidatorContext.class);
    }

    @Nested
    @DisplayName("isValid 메서드는")
    class DescribeIsValid {

        @Test
        @DisplayName("레시피가 존재하면 true를 반환한다")
        void shouldReturnTrueWhenRecipeExists() {
            // Given
            UUID recipeId = UUID.randomUUID();
            doReturn(true).when(recipeInfoService).exists(recipeId);

            // When
            boolean result = validator.isValid(recipeId, context);

            // Then
            assertThat(result).isTrue();
            verify(recipeInfoService).exists(recipeId);
        }

        @Test
        @DisplayName("레시피가 존재하지 않으면 false를 반환하고 에러 메시지를 설정한다")
        void shouldReturnFalseWhenRecipeNotExists() {
            // Given
            UUID recipeId = UUID.randomUUID();
            doReturn(false).when(recipeInfoService).exists(recipeId);

            ConstraintValidatorContext.ConstraintViolationBuilder builder =
                    mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            doReturn(builder).when(context).buildConstraintViolationWithTemplate(anyString());

            // When
            boolean result = validator.isValid(recipeId, context);

            // Then
            assertThat(result).isFalse();
            verify(recipeInfoService).exists(recipeId);
            verify(context).disableDefaultConstraintViolation();
        }
    }
}
