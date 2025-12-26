package com.cheftory.api.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.entity.CreditUserBalance;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("CreditTxService")
class CreditTxServiceTest {

  private CreditUserBalanceRepository balanceRepository;
  private CreditTransactionRepository transactionRepository;
  private Clock clock;

  private CreditTxService creditTxService;

  @BeforeEach
  void setUp() {
    balanceRepository = mock(CreditUserBalanceRepository.class);
    transactionRepository = mock(CreditTransactionRepository.class);
    clock = mock(Clock.class);

    creditTxService = new CreditTxService(balanceRepository, transactionRepository, clock);

    doReturn(LocalDateTime.of(2024, 1, 1, 0, 0)).when(clock).now();
  }

  @Nested
  @DisplayName("grantTx")
  class GrantTx {

    @Test
    @DisplayName("balance가 없으면 생성 후 amount를 더하고 balance를 저장한다")
    void createsBalanceAndGrants() {
      UUID userId = UUID.randomUUID();
      Credit credit = Credit.signupBonus(userId);

      doReturn(Optional.empty()).when(balanceRepository).findById(userId);

      CreditUserBalance created = CreditUserBalance.create(userId);
      doReturn(created).when(balanceRepository).saveAndFlush(any(CreditUserBalance.class));

      creditTxService.grantTx(credit);

      verify(transactionRepository, times(1)).save(any());

      ArgumentCaptor<CreditUserBalance> captor = ArgumentCaptor.forClass(CreditUserBalance.class);
      verify(balanceRepository, times(1)).save(captor.capture());
      assertThat(captor.getValue().getUserId()).isEqualTo(userId);
      assertThat(captor.getValue().getBalance()).isEqualTo(100L);
    }

    @Test
    @DisplayName("멱등키가 이미 처리된 경우(tx save에서 DIVE + exists=true) balance 변경 없이 return 한다")
    void idempotencySkipsBalanceUpdate() {
      UUID userId = UUID.randomUUID();
      Credit credit = Credit.signupBonus(userId);

      CreditUserBalance balance = CreditUserBalance.create(userId);
      balance.apply(200L);

      doReturn(Optional.of(balance)).when(balanceRepository).findById(userId);

      doThrow(new DataIntegrityViolationException("duplicate"))
          .when(transactionRepository)
          .save(any());

      doReturn(true).when(transactionRepository).existsByIdempotencyKey(credit.idempotencyKey());

      creditTxService.grantTx(credit);

      verify(balanceRepository, never()).save(any());
      assertThat(balance.getBalance()).isEqualTo(200L);
    }

    @Test
    @DisplayName("tx save에서 DIVE가 났지만 exists=false면 예외를 다시 던진다")
    void idempotencyRethrowsIfNotActuallyProcessed() {
      UUID userId = UUID.randomUUID();
      Credit credit = Credit.signupBonus(userId);

      CreditUserBalance balance = CreditUserBalance.create(userId);
      doReturn(Optional.of(balance)).when(balanceRepository).findById(userId);

      doThrow(new DataIntegrityViolationException("duplicate?"))
          .when(transactionRepository)
          .save(any());

      doReturn(false).when(transactionRepository).existsByIdempotencyKey(credit.idempotencyKey());

      assertThatThrownBy(() -> creditTxService.grantTx(credit))
          .isInstanceOf(DataIntegrityViolationException.class);

      verify(balanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveAndFlush 레이스로 DIVE가 나면 다시 findById로 가져와서 진행한다")
    void loadOrCreateBalanceRaceCondition() {
      UUID userId = UUID.randomUUID();
      Credit credit = Credit.signupBonus(userId);

      CreditUserBalance existing = CreditUserBalance.create(userId);

      doReturn(Optional.empty(), Optional.of(existing)).when(balanceRepository).findById(userId);
      doThrow(new DataIntegrityViolationException("race"))
          .when(balanceRepository)
          .saveAndFlush(any(CreditUserBalance.class));

      creditTxService.grantTx(credit);

      verify(balanceRepository, times(2)).findById(userId);
      verify(balanceRepository, times(1)).saveAndFlush(any(CreditUserBalance.class));

      ArgumentCaptor<CreditUserBalance> captor = ArgumentCaptor.forClass(CreditUserBalance.class);
      verify(balanceRepository, times(1)).save(captor.capture());
      assertThat(captor.getValue().getBalance()).isEqualTo(100L);
    }
  }

  @Nested
  @DisplayName("spendTx")
  class SpendTx {

    @Test
    @DisplayName("기존 balance에서 amount를 차감하고 balance를 저장한다")
    void spendsFromExistingBalance() {
      UUID userId = UUID.randomUUID();

      CreditUserBalance balance = CreditUserBalance.create(userId);
      balance.apply(200L);

      Credit credit = Credit.recipeCreate(userId, UUID.randomUUID(), 50L);

      doReturn(Optional.of(balance)).when(balanceRepository).findById(userId);

      creditTxService.spendTx(credit);

      verify(transactionRepository, times(1)).save(any());

      ArgumentCaptor<CreditUserBalance> captor = ArgumentCaptor.forClass(CreditUserBalance.class);
      verify(balanceRepository, times(1)).save(captor.capture());
      assertThat(captor.getValue().getBalance()).isEqualTo(150L);
    }
  }
}
