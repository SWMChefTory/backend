package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.exception.RecipeException;
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
    @DisplayName("createAndCharge")
    class CreateAndCharge {

        @Test
        @DisplayName("북마크가 새로 생성되면 credit 차감을 시도한다")
        void shouldSpendCreditWhenBookmarkCreated() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            long creditCost = 100L;

            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            when(recipeInfo.getId()).thenReturn(recipeId);
            when(recipeInfo.getCreditCost()).thenReturn(creditCost);
            when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
            when(recipeBookmarkService.create(userId, recipeId)).thenReturn(true);

            sut.createAndCharge(userId, recipeId);

            verify(recipeBookmarkService).create(userId, recipeId);
            verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
            verify(recipeBookmarkService, never()).delete(userId, recipeId);
        }

        @Test
        @DisplayName("이미 북마크가 존재하면 credit 차감을 하지 않는다")
        void shouldNotSpendCreditWhenBookmarkAlreadyExists() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            long creditCost = 100L;

            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            when(recipeInfo.getId()).thenReturn(recipeId);
            when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
            when(recipeBookmarkService.create(userId, recipeId)).thenReturn(false);

            sut.createAndCharge(userId, recipeId);

            verify(recipeBookmarkService).create(userId, recipeId);
            verify(creditPort, never()).spendRecipeCreate(userId, recipeId, creditCost);
        }

        @Test
        @DisplayName("credit 부족이면 북마크 삭제 후 CREDIT_INSUFFICIENT로 변환해 던진다")
        void shouldDeleteBookmarkAndThrowCreditInsufficient() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            long creditCost = 100L;

            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            when(recipeInfo.getId()).thenReturn(recipeId);
            when(recipeInfo.getCreditCost()).thenReturn(creditCost);
            when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
            when(recipeBookmarkService.create(userId, recipeId)).thenReturn(true);

            doThrow(new CreditException(CreditErrorCode.CREDIT_INSUFFICIENT))
                .when(creditPort)
                .spendRecipeCreate(userId, recipeId, creditCost);

            assertThatThrownBy(() -> sut.createAndCharge(userId, recipeId))
                .isInstanceOf(RecipeException.class)
                .hasFieldOrPropertyWithValue("errorMessage", CreditErrorCode.CREDIT_INSUFFICIENT);

            verify(recipeBookmarkService).delete(userId, recipeId);
        }

        @Test
        @DisplayName("credit 동시성 충돌이면 북마크 삭제 후 CREDIT_CONCURRENCY_CONFLICT로 변환해 던진다")
        void shouldDeleteBookmarkAndThrowConcurrencyConflict() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            long creditCost = 100L;

            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            when(recipeInfo.getId()).thenReturn(recipeId);
            when(recipeInfo.getCreditCost()).thenReturn(creditCost);
            when(recipeInfoService.get(recipeId)).thenReturn(recipeInfo);
            when(recipeBookmarkService.create(userId, recipeId)).thenReturn(true);

            doThrow(new CreditException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT))
                .when(creditPort)
                .spendRecipeCreate(userId, recipeId, creditCost);

            assertThatThrownBy(() -> sut.createAndCharge(userId, recipeId))
                .isInstanceOf(RecipeException.class)
                .hasFieldOrPropertyWithValue("errorMessage", CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);

            verify(recipeBookmarkService).delete(userId, recipeId);
        }
    }
}
