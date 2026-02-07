package com.cheftory.api.credit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Credit 도메인 테스트")
class CreditTest {

    @Test
    @DisplayName("tutorial - 튜토리얼 크레딧이 생성된다")
    void tutorial_creates_credit() {
        UUID userId = UUID.randomUUID();

        Credit credit = Credit.tutorial(userId);

        assertThat(credit.userId()).isEqualTo(userId);
        assertThat(credit.amount()).isEqualTo(30L);
        assertThat(credit.reason()).isEqualTo(CreditReason.TUTORIAL);
        assertThat(credit.idempotencyKey()).isEqualTo("tutorial:" + userId);
    }

    @Test
    @DisplayName("share - 공유 크레딧이 생성된다")
    void share_creates_credit() {
        UUID userId = UUID.randomUUID();
        int count = 2;
        String date = LocalDate.now().toString();

        Credit credit = Credit.share(userId, count);

        assertThat(credit.userId()).isEqualTo(userId);
        assertThat(credit.amount()).isEqualTo(10L);
        assertThat(credit.reason()).isEqualTo(CreditReason.SHARE);
        assertThat(credit.idempotencyKey()).isEqualTo("share:" + userId + ":" + date + ":" + count);
    }
}
