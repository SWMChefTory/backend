package com.cheftory.api.credit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreditUserBalance Entity")
class CreditUserBalanceTest {

  @Test
  @DisplayName("create - userId가 null이면 CREDIT_INVALID_USER 예외")
  void create_shouldThrowWhenUserIdNull() {
    assertThatThrownBy(() -> CreditUserBalance.create(null))
        .isInstanceOf(CreditException.class)
        .satisfies(
            ex -> {
              CreditException e = (CreditException) ex;
              assertThat(e.getErrorMessage()).isEqualTo(CreditErrorCode.CREDIT_INVALID_USER);
            });
  }

  @Test
  @DisplayName("apply - 잔액이 음수가 되면 CREDIT_INSUFFICIENT 예외")
  void apply_shouldThrowWhenBalanceNegative() {
    UUID userId = UUID.randomUUID();
    CreditUserBalance balance = CreditUserBalance.create(userId);

    assertThatThrownBy(() -> balance.apply(-1))
        .isInstanceOf(CreditException.class)
        .satisfies(
            ex -> {
              CreditException e = (CreditException) ex;
              assertThat(e.getErrorMessage()).isEqualTo(CreditErrorCode.CREDIT_INSUFFICIENT);
            });
  }

  @Test
  @DisplayName("apply - 정상적으로 증감된다")
  void apply_shouldUpdateBalance() {
    UUID userId = UUID.randomUUID();
    CreditUserBalance balance = CreditUserBalance.create(userId);

    balance.apply(10);
    assertThat(balance.getBalance()).isEqualTo(10);

    balance.apply(-3);
    assertThat(balance.getBalance()).isEqualTo(7);
  }
}
