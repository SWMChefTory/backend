package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBookmarkFacade 테스트")
class RecipeBookmarkFacadeTest {

    private RecipeBookmarkService recipeBookmarkService;
    private RecipeInfoService recipeInfoService;
    private RecipeCreditPort creditPort;

    private RecipeBookmarkFacade sut;

    @BeforeEach
    void setUp() {
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        creditPort = mock(RecipeCreditPort.class);

        sut = new RecipeBookmarkFacade(recipeBookmarkService, recipeInfoService, creditPort);
    }

    @Nested
    @DisplayName("북마크 생성 및 과금 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 새로운 북마크 생성 시")
        class GivenNewBookmark {
            UUID userId;
            UUID recipeId;
            long creditCost;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException, RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                creditCost = 100L;

                recipeInfo = mock(RecipeInfo.class);
                when(recipeInfo.getId()).thenReturn(recipeId);
                when(recipeInfo.getCreditCost()).thenReturn(creditCost);
                when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
                when(recipeBookmarkService.create(userId, recipeId)).thenReturn(true);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws CreditException, RecipeBookmarkException, RecipeInfoException {
                    sut.create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 북마크를 생성하고 크레딧을 차감한다")
                void thenCreatesAndCharges() throws CreditException, RecipeBookmarkException {
                    verify(recipeBookmarkService).create(userId, recipeId);
                    verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
                    verify(recipeBookmarkService, never()).delete(userId, recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 존재하는 북마크일 때")
        class GivenExistingBookmark {
            UUID userId;
            UUID recipeId;
            long creditCost;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException, RecipeBookmarkException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                creditCost = 100L;

                recipeInfo = mock(RecipeInfo.class);
                when(recipeInfo.getId()).thenReturn(recipeId);
                when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
                when(recipeBookmarkService.create(userId, recipeId)).thenReturn(false);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws CreditException, RecipeBookmarkException, RecipeInfoException {
                    sut.create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 북마크만 생성(활성화)하고 크레딧은 차감하지 않는다")
                void thenCreatesOnly() throws CreditException, RecipeBookmarkException {
                    verify(recipeBookmarkService).create(userId, recipeId);
                    verify(creditPort, never()).spendRecipeCreate(userId, recipeId, creditCost);
                }
            }
        }

        @Nested
        @DisplayName("Given - 크레딧이 부족할 때")
        class GivenInsufficientCredit {
            UUID userId;
            UUID recipeId;
            long creditCost;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException, RecipeBookmarkException, CreditException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                creditCost = 100L;

                recipeInfo = mock(RecipeInfo.class);
                when(recipeInfo.getId()).thenReturn(recipeId);
                when(recipeInfo.getCreditCost()).thenReturn(creditCost);
                when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
                when(recipeBookmarkService.create(userId, recipeId)).thenReturn(true);

                doThrow(new CreditException(CreditErrorCode.CREDIT_INSUFFICIENT))
                        .when(creditPort)
                        .spendRecipeCreate(userId, recipeId, creditCost);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - CREDIT_INSUFFICIENT 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> sut.create(userId, recipeId))
                            .isInstanceOf(CreditException.class)
                            .hasFieldOrPropertyWithValue("error", CreditErrorCode.CREDIT_INSUFFICIENT);
                }
            }
        }

        @Nested
        @DisplayName("Given - 크레딧 동시성 충돌 발생 시")
        class GivenConcurrencyConflict {
            UUID userId;
            UUID recipeId;
            long creditCost;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException, RecipeBookmarkException, CreditException {
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                creditCost = 100L;

                recipeInfo = mock(RecipeInfo.class);
                when(recipeInfo.getId()).thenReturn(recipeId);
                when(recipeInfo.getCreditCost()).thenReturn(creditCost);
                when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
                when(recipeBookmarkService.create(userId, recipeId)).thenReturn(true);

                doThrow(new CreditException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT))
                        .when(creditPort)
                        .spendRecipeCreate(userId, recipeId, creditCost);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - CREDIT_CONCURRENCY_CONFLICT 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> sut.create(userId, recipeId))
                            .isInstanceOf(CreditException.class)
                            .hasFieldOrPropertyWithValue("error", CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
                }
            }
        }
    }
}
