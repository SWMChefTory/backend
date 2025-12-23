package com.cheftory.api.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.DbContextTest;
import com.cheftory.api.credit.entity.CreditUserBalance;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@DisplayName("CreditUserBalanceRepositoryTest")
class CreditUserBalanceRepositoryTest extends DbContextTest {

  @Autowired private CreditUserBalanceRepository balanceRepository;
  @Autowired private EntityManager em;

  @Nested
  @DisplayName("저장/조회")
  class SaveAndFind {

    @Test
    @DisplayName("새 balance 저장 후 findById로 조회된다")
    void save_then_find() {
      UUID userId = UUID.randomUUID();
      CreditUserBalance saved = balanceRepository.saveAndFlush(CreditUserBalance.create(userId));

      em.clear();

      CreditUserBalance found = balanceRepository.findById(userId).orElseThrow();
      assertThat(found.getUserId()).isEqualTo(saved.getUserId());
      assertThat(found.getBalance()).isEqualTo(0L);
    }

    @Test
    @DisplayName("apply 후 저장하면 balance가 반영된다")
    void apply_then_save() {
      UUID userId = UUID.randomUUID();
      CreditUserBalance balance = CreditUserBalance.create(userId);
      balance.apply(123L);

      balanceRepository.saveAndFlush(balance);
      em.clear();

      CreditUserBalance found = balanceRepository.findById(userId).orElseThrow();
      assertThat(found.getBalance()).isEqualTo(123L);
    }
  }

  @Nested
  @DisplayName("낙관적 락(@Version)")
  class OptimisticLock {

    @Test
    @DisplayName("동일 row를 서로 다른 버전으로 저장하면 OptimisticLock 예외가 난다")
    void optimistic_lock_conflict() {
      UUID userId = UUID.randomUUID();
      balanceRepository.saveAndFlush(CreditUserBalance.create(userId));
      em.clear();

      CreditUserBalance b1 = balanceRepository.findById(userId).orElseThrow();
      em.clear();
      CreditUserBalance b2 = balanceRepository.findById(userId).orElseThrow();

      b1.apply(10L);
      balanceRepository.saveAndFlush(b1);

      b2.apply(1L);
      assertThatThrownBy(() -> balanceRepository.saveAndFlush(b2))
          .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
  }
}