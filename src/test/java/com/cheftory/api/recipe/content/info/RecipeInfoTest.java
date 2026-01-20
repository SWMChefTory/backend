package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.info.entity.ProcessStep;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeTest")
public class RecipeInfoTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(LocalDateTime.now()).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 생성")
    class CreateRecipeInfo {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            @Nested
            @DisplayName("When - 레시피를 생성하면")
            class WhenCreateRecipeInfo {

                private RecipeInfo recipeInfo;

                @BeforeEach
                void setUp() {
                    recipeInfo = RecipeInfo.create(clock);
                }

                @DisplayName("Then - 레시피가 생성된다")
                @Test
                void thenRecipeIsCreated() {
                    assertThat(recipeInfo).isNotNull();
                    assertThat(recipeInfo.getId()).isNotNull();
                    assertThat(recipeInfo.getProcessStep()).isEqualTo(ProcessStep.READY);
                    assertThat(recipeInfo.getViewCount()).isEqualTo(0);
                    assertThat(recipeInfo.getCreatedAt()).isNotNull();
                    assertThat(recipeInfo.getUpdatedAt()).isNotNull();
                    assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 상태 변경")
    class ChangeRecipeInfoStatus {

        @Nested
        @DisplayName("Given - 레시피가 생성되어 있을 때")
        class GivenRecipeInfoCreated {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
            }

            @Nested
            @DisplayName("When - 레시피 상태를 성공으로 변경하면")
            class WhenChangeStatusToSuccess {

                @BeforeEach
                void setUp() {
                    recipeInfo.success(clock);
                }

                @DisplayName("Then - 레시피 상태가 성공으로 변경된다")
                @Test
                void thenRecipeStatusIsSuccess() {
                    assertThat(recipeInfo.isSuccess()).isTrue();
                    assertThat(recipeInfo.isFailed()).isFalse();
                    assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
                }
            }

            @Nested
            @DisplayName("When - 레시피 상태를 실패로 변경하면")
            class WhenChangeStatusToFailed {

                @BeforeEach
                void setUp() {
                    recipeInfo.failed(clock);
                }

                @DisplayName("Then - 레시피 상태가 실패로 변경된다")
                @Test
                void thenRecipeStatusIsFailed() {
                    assertThat(recipeInfo.isFailed()).isTrue();
                    assertThat(recipeInfo.isSuccess()).isFalse();
                    assertThat(recipeInfo.isBlocked()).isFalse();
                    assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 상태 확인")
    class CheckRecipeInfoStatus {

        @Nested
        @DisplayName("Given - SUCCESS 상태의 레시피가 있을 때")
        class GivenSuccessRecipeInfo {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
                recipeInfo.success(clock);
            }

            @Test
            @DisplayName("Then - isSuccess는 true, isFailed와 isBlocked는 false를 반환한다")
            void thenSuccessCheckMethodsWork() {
                assertThat(recipeInfo.isSuccess()).isTrue();
                assertThat(recipeInfo.isFailed()).isFalse();
                assertThat(recipeInfo.isBlocked()).isFalse();
            }
        }

        @Nested
        @DisplayName("Given - FAILED 상태의 레시피가 있을 때")
        class GivenFailedRecipeInfo {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
                recipeInfo.failed(clock);
            }

            @Test
            @DisplayName("Then - isFailed는 true, isSuccess와 isBlocked는 false를 반환한다")
            void thenFailedCheckMethodsWork() {
                assertThat(recipeInfo.isFailed()).isTrue();
                assertThat(recipeInfo.isSuccess()).isFalse();
                assertThat(recipeInfo.isBlocked()).isFalse();
            }
        }

        @Nested
        @DisplayName("Given - IN_PROGRESS 상태의 레시피가 있을 때")
        class GivenInProgressRecipeInfo {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                recipeInfo = RecipeInfo.create(clock);
            }

            @Test
            @DisplayName("Then - 모든 상태 체크 메서드가 false를 반환한다")
            void thenInProgressCheckMethodsWork() {
                assertThat(recipeInfo.isSuccess()).isFalse();
                assertThat(recipeInfo.isFailed()).isFalse();
                assertThat(recipeInfo.isBlocked()).isFalse();
                assertThat(recipeInfo.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
            }
        }
    }
}
