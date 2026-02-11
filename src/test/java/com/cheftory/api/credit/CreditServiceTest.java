package com.cheftory.api.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.entity.CreditUserBalance;
import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@DisplayName("CreditService Test")
class CreditServiceTest {

    private CreditTxService creditTxService;
    private CreditUserBalanceRepository balanceRepository;
    private CreditService creditService;

    @BeforeEach
    void setUp() {
        creditTxService = mock(CreditTxService.class);
        balanceRepository = mock(CreditUserBalanceRepository.class);
        creditService = new CreditService(creditTxService, balanceRepository);
    }

    @Test
    @DisplayName("getBalance - 없으면 0 반환")
    void getBalance_shouldReturnZeroWhenNotExists() {
        UUID userId = UUID.randomUUID();
        doReturn(Optional.empty()).when(balanceRepository).findById(userId);

        long balance = creditService.getBalance(userId);

        assertThat(balance).isEqualTo(0L);
    }

    @Test
    @DisplayName("getBalance - 있으면 balance 반환")
    void getBalance_shouldReturnBalance() throws CreditException {
        UUID userId = UUID.randomUUID();
        CreditUserBalance entity = CreditUserBalance.create(userId);
        entity.apply(55);

        doReturn(Optional.of(entity)).when(balanceRepository).findById(userId);

        long balance = creditService.getBalance(userId);

        assertThat(balance).isEqualTo(55L);
    }

    @Test
    @DisplayName("grant - txService.grantTx 위임")
    void grant_shouldDelegate() throws CreditException {
        Credit credit = Credit.signupBonus(UUID.randomUUID());

        creditService.grant(credit);

        verify(creditTxService).grantTx(credit);
    }

    @Test
    @DisplayName("spend - txService.spendTx 위임")
    void spend_shouldDelegate() throws CreditException {
        Credit credit = Credit.signupBonus(UUID.randomUUID());

        creditService.spend(credit);

        verify(creditTxService).spendTx(credit);
    }

    @Test
    @DisplayName("recover - 낙관적 잠금 재시도 실패 시 CREDIT_CONCURRENCY_CONFLICT 예외")
    void recover_shouldThrowConcurrencyConflict() throws CreditException {
        UUID userId = UUID.randomUUID();
        Credit credit = Credit.signupBonus(userId);
        ObjectOptimisticLockingFailureException exception =
                new ObjectOptimisticLockingFailureException("optimistic lock failed", null);

        assertThatThrownBy(() -> creditService.recover(exception, credit))
                .isInstanceOf(CreditException.class)
                .extracting(e -> ((CreditException) e).getError())
                .isEqualTo(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
    }
}
