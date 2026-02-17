package com.cheftory.api.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.CreditTransactionRepository;
import com.cheftory.api.credit.CreditTxService;
import com.cheftory.api.credit.CreditUserBalanceRepository;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.entity.CreditUserBalance;
import com.cheftory.api.credit.exception.CreditException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest
@DisplayName("CreditTxService 트랜잭션 테스트")
class CreditTxServiceTxTest {

    @Autowired
    private CreditTxService creditTxService;

    @MockitoBean
    private CreditUserBalanceRepository balanceRepository;

    @MockitoBean
    private CreditTransactionRepository transactionRepository;

    @MockitoBean
    private Clock clock;

    @MockitoSpyBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        reset(balanceRepository, transactionRepository, clock, transactionManager);
        doReturn(LocalDateTime.of(2024, 1, 1, 0, 0)).when(clock).now();
    }

    @Test
    @DisplayName("spendTx: checked 예외 발생 시 rollback")
    void rollbacksOnCheckedException() throws CreditException {
        UUID userId = UUID.randomUUID();
        CreditUserBalance emptyBalance = CreditUserBalance.create(userId);
        doReturn(Optional.of(emptyBalance)).when(balanceRepository).findById(userId);

        Credit credit = Credit.recipeCreate(userId, UUID.randomUUID(), 1L);

        assertThatThrownBy(() -> creditTxService.spendTx(credit)).isInstanceOf(CreditException.class);
        verify(transactionManager).rollback(org.mockito.ArgumentMatchers.any());
        verify(transactionManager, never()).commit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("grantTx: 예외 없으면 commit")
    void commitsOnSuccess() throws CreditException {
        UUID userId = UUID.randomUUID();
        doReturn(Optional.empty()).when(balanceRepository).findById(userId);
        doReturn(CreditUserBalance.create(userId))
                .when(balanceRepository)
                .saveAndFlush(org.mockito.ArgumentMatchers.any(CreditUserBalance.class));

        Credit credit = Credit.signupBonus(userId);
        creditTxService.grantTx(credit);

        verify(transactionManager).commit(org.mockito.ArgumentMatchers.any());
        verify(transactionManager, never()).rollback(org.mockito.ArgumentMatchers.any());
    }
}
