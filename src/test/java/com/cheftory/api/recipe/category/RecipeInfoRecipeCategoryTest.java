package com.cheftory.api.recipe.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.entity.RecipeCategoryStatus;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCategory 엔티티")
public class RecipeInfoRecipeCategoryTest {

    @Nested
    @DisplayName("레시피 카테고리 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            String categoryName;
            UUID userId;
            Clock clock;

            @BeforeEach
            void setUp() {
                categoryName = "한식";
                userId = UUID.randomUUID();
                clock = new Clock();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeCategory recipeCategory;

                @BeforeEach
                void setUp() throws RecipeCategoryException {
                    recipeCategory = RecipeCategory.create(clock, categoryName, userId);
                }

                @Test
                @DisplayName("Then - 카테고리가 생성된다")
                void thenCreated() {
                    assertThat(recipeCategory.getId()).isNotNull();
                    assertThat(recipeCategory.getName()).isEqualTo("한식");
                    assertThat(recipeCategory.getUserId()).isEqualTo(userId);
                    assertThat(recipeCategory.getCreatedAt()).isBeforeOrEqualTo(clock.now());
                    assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.ACTIVE);
                }
            }
        }

        @Nested
        @DisplayName("Given - 이름이 비어있을 때")
        class GivenEmptyName {
            String emptyCategoryName;
            UUID userId;
            Clock clock;

            @BeforeEach
            void setUp() {
                emptyCategoryName = "";
                userId = UUID.randomUUID();
                clock = new Clock();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - NAME_EMPTY 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> RecipeCategory.create(clock, emptyCategoryName, userId))
                            .isInstanceOfSatisfying(RecipeCategoryException.class, ex -> assertThat(ex.getError())
                                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY));
                }
            }
        }

        @Nested
        @DisplayName("Given - 이름이 null일 때")
        class GivenNullName {
            String nullCategoryName;
            UUID userId;
            Clock clock;

            @BeforeEach
            void setUp() {
                nullCategoryName = null;
                userId = UUID.randomUUID();
                clock = new Clock();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - NAME_EMPTY 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> RecipeCategory.create(clock, nullCategoryName, userId))
                            .isInstanceOfSatisfying(RecipeCategoryException.class, ex -> assertThat(ex.getError())
                                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY));
                }
            }
        }

        @Nested
        @DisplayName("Given - 이름이 공백일 때")
        class GivenBlankName {
            String blankCategoryName;
            UUID userId;
            Clock clock;

            @BeforeEach
            void setUp() {
                blankCategoryName = "   ";
                userId = UUID.randomUUID();
                clock = new Clock();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - NAME_EMPTY 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> RecipeCategory.create(clock, blankCategoryName, userId))
                            .isInstanceOfSatisfying(RecipeCategoryException.class, ex -> assertThat(ex.getError())
                                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY));
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 카테고리 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 활성 상태의 카테고리가 있을 때")
        class GivenActiveCategory {
            RecipeCategory recipeCategory;
            String categoryName;
            UUID userId;
            Clock clock;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                categoryName = "한식";
                userId = UUID.randomUUID();
                clock = new Clock();
                recipeCategory = RecipeCategory.create(clock, categoryName, userId);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() {
                    recipeCategory.delete();
                }

                @Test
                @DisplayName("Then - 상태가 DELETED로 변경된다")
                void thenStatusDeleted() {
                    assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.DELETED);
                }

                @Test
                @DisplayName("Then - 다른 속성은 유지된다")
                void thenPropertiesKept() {
                    assertThat(recipeCategory.getName()).isEqualTo(categoryName);
                    assertThat(recipeCategory.getUserId()).isEqualTo(userId);
                    assertThat(recipeCategory.getId()).isNotNull();
                    assertThat(recipeCategory.getCreatedAt()).isNotNull();
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 삭제된 카테고리가 있을 때")
        class GivenDeletedCategory {
            RecipeCategory recipeCategory;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                Clock clock = new Clock();
                recipeCategory = RecipeCategory.create(clock, "한식", UUID.randomUUID());
                recipeCategory.delete();
            }

            @Nested
            @DisplayName("When - 다시 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() {
                    recipeCategory.delete();
                }

                @Test
                @DisplayName("Then - 상태는 여전히 DELETED이다")
                void thenStatusRemainsDeleted() {
                    assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.DELETED);
                }
            }
        }
    }
}
