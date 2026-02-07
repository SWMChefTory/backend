package com.cheftory.api.credit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.credit.entity.CreditReason;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreditAdapter 테스트")
class RecipeCreditAdapterTest {

    private CreditService creditService;
    private RecipeCreditAdapter recipeCreditAdapter;

    @BeforeEach
    void setUp() {
        creditService = mock(CreditService.class);
        recipeCreditAdapter = new RecipeCreditAdapter(creditService);
    }

    @Test
    @DisplayName("spendRecipeCreate - 레시피 생성 크레딧 차감을 CreditService에 위임한다")
    void spendRecipeCreate_shouldDelegateToCreditService() {
        // given
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        long cost = 100L;

        // when
        recipeCreditAdapter.spendRecipeCreate(userId, recipeId, cost);

        // then
        verify(creditService)
                .spend(argThat(credit -> credit.userId().equals(userId)
                        && credit.reason() == CreditReason.RECIPE_CREATE
                        && credit.amount() == cost
                        && credit.idempotencyKey().contains("recipe-create:" + userId + ":" + recipeId)));
    }

    @Test
    @DisplayName("refundRecipeCreate - 레시피 생성 크레딧 환불을 CreditService에 위임한다")
    void refundRecipeCreate_shouldDelegateToCreditService() {
        // given
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        long cost = 100L;

        // when
        recipeCreditAdapter.refundRecipeCreate(userId, recipeId, cost);

        // then
        verify(creditService)
                .grant(argThat(credit -> credit.userId().equals(userId)
                        && credit.reason() == CreditReason.RECIPE_CREATE_REFUND
                        && credit.amount() == cost
                        && credit.idempotencyKey().contains("recipe-create-refund:" + userId + ":" + recipeId)));
    }
}
