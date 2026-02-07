package com.cheftory.api.credit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.credit.entity.CreditReason;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserCreditAdapter 테스트")
class UserCreditAdapterTest {

    private CreditService creditService;
    private UserCreditAdapter userCreditAdapter;

    @BeforeEach
    void setUp() {
        creditService = mock(CreditService.class);
        userCreditAdapter = new UserCreditAdapter(creditService);
    }

    @Test
    @DisplayName("grantUserShare - 공유 크레딧 지급을 CreditService에 위임한다")
    void grantUserShare_shouldDelegateToCreditService() {
        // given
        UUID userId = UUID.randomUUID();
        int count = 1;

        // when
        userCreditAdapter.grantUserShare(userId, count);

        // then
        verify(creditService).grant(argThat(credit -> 
            credit.userId().equals(userId) && 
            credit.reason() == CreditReason.SHARE &&
            credit.amount() == 10L &&
            credit.idempotencyKey().contains("share:" + userId + ":") &&
            credit.idempotencyKey().endsWith(":" + count)
        ));
    }

    @Test
    @DisplayName("grantUserTutorial - 튜토리얼 크레딧 지급을 CreditService에 위임한다")
    void grantUserTutorial_shouldDelegateToCreditService() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        userCreditAdapter.grantUserTutorial(userId);

        // then
        verify(creditService).grant(argThat(credit -> 
            credit.userId().equals(userId) && 
            credit.reason() == CreditReason.TUTORIAL &&
            credit.amount() == 30L
        ));
    }
}
