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
    @DisplayName("유효성 검증 (isValid)")
    class IsValid {

        @Nested
        @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
        class GivenExistingRecipeId {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                doReturn(true).when(recipeInfoService).exists(recipeId);
            }

            @Nested
            @DisplayName("When - 검증을 요청하면")
            class WhenValidating {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = validator.isValid(recipeId, context);
                }

                @Test
                @DisplayName("Then - true를 반환한다")
                void thenReturnsTrue() {
                    assertThat(result).isTrue();
                    verify(recipeInfoService).exists(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistingRecipeId {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                doReturn(false).when(recipeInfoService).exists(recipeId);

                ConstraintValidatorContext.ConstraintViolationBuilder builder =
                        mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
                doReturn(builder).when(context).buildConstraintViolationWithTemplate(anyString());
            }

            @Nested
            @DisplayName("When - 검증을 요청하면")
            class WhenValidating {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = validator.isValid(recipeId, context);
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    assertThat(result).isFalse();
                    verify(recipeInfoService).exists(recipeId);
                    verify(context).disableDefaultConstraintViolation();
                }
            }
        }
    }
}
