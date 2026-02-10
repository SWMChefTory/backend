package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeInfo 엔티티")
public class RecipeInfoTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(LocalDateTime.now()).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeInfo recipeInfo;

                @BeforeEach
                void setUp() {
                    recipeInfo = RecipeInfo.create(clock);
                }

                @Test
                @DisplayName("Then - 초기 상태로 생성된다")
                void thenCreated() {
                    assertThat(recipeInfo).isNotNull();
                    assertThat(recipeInfo.getId()).isNotNull();
                    assertThat(recipeInfo.getViewCount()).isEqualTo(0);
                    assertThat(recipeInfo.getCreatedAt()).isNotNull();
                    assertThat(recipeInfo.getUpdatedAt()).isNotNull();
                    assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
                }
            }
        }
    }

    @Nested
    @DisplayName("상태 변경 (success, failed)")
    class StatusChange {

        @Nested
        @DisplayName("Given - 레시피가 생성되어 있을 때")
        class GivenCreated {
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
            }

            @Nested
            @DisplayName("When - 성공으로 변경하면")
            class WhenSuccess {

                @BeforeEach
                void setUp() {
                    recipeInfo.success(clock);
                }

                @Test
                @DisplayName("Then - SUCCESS 상태가 된다")
                void thenSuccess() {
                    assertThat(recipeInfo.isSuccess()).isTrue();
                    assertThat(recipeInfo.isFailed()).isFalse();
                    assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
                }
            }

            @Nested
            @DisplayName("When - 실패로 변경하면")
            class WhenFailed {

                @BeforeEach
                void setUp() {
                    recipeInfo.failed(clock);
                }

                @Test
                @DisplayName("Then - FAILED 상태가 된다")
                void thenFailed() {
                    assertThat(recipeInfo.isFailed()).isTrue();
                    assertThat(recipeInfo.isSuccess()).isFalse();
                    assertThat(recipeInfo.isBlocked()).isFalse();
                    assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
                }
            }
        }
    }

    @Nested
    @DisplayName("상태 확인 (isSuccess, isFailed, isBlocked)")
    class StatusCheck {

        @Nested
        @DisplayName("Given - SUCCESS 상태일 때")
        class GivenSuccess {
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
                recipeInfo.success(clock);
            }

            @Test
            @DisplayName("Then - isSuccess만 true를 반환한다")
            void thenIsSuccessTrue() {
                assertThat(recipeInfo.isSuccess()).isTrue();
                assertThat(recipeInfo.isFailed()).isFalse();
                assertThat(recipeInfo.isBlocked()).isFalse();
            }
        }

        @Nested
        @DisplayName("Given - FAILED 상태일 때")
        class GivenFailed {
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
                recipeInfo.failed(clock);
            }

            @Test
            @DisplayName("Then - isFailed만 true를 반환한다")
            void thenIsFailedTrue() {
                assertThat(recipeInfo.isFailed()).isTrue();
                assertThat(recipeInfo.isSuccess()).isFalse();
                assertThat(recipeInfo.isBlocked()).isFalse();
            }
        }

        @Nested
        @DisplayName("Given - IN_PROGRESS 상태일 때")
        class GivenInProgress {
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
            }

            @Test
            @DisplayName("Then - 모든 상태 확인 메서드가 false를 반환한다")
            void thenAllFalse() {
                assertThat(recipeInfo.isSuccess()).isFalse();
                assertThat(recipeInfo.isFailed()).isFalse();
                assertThat(recipeInfo.isBlocked()).isFalse();
                assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
            }
        }
    }
}
