package com.cheftory.api.credit.entity;

import com.cheftory.api._common.Clock;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 크레딧 거래 이력을 나타내는 엔티티.
 * 멱등성 키를 통해 중복 거래를 방지합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uq_credit_tx_idempotency_key", columnNames = "idempotency_key")})
public class CreditTransaction {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CreditReason reason;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 크레딧 지급 거래를 생성합니다.
     *
     * @param credit 크레딧 정보
     * @param clock 현재 시간 제공자
     * @return 크레딧 지급 거래 엔티티
     */
    public static CreditTransaction grant(Credit credit, Clock clock) {
        return new CreditTransaction(
                UUID.randomUUID(),
                credit.userId(),
                CreditTransactionType.GRANT,
                credit.reason(),
                credit.amount(),
                credit.idempotencyKey(),
                clock.now());
    }

    /**
     * 크레딧 사용 거래를 생성합니다.
     *
     * @param credit 크레딧 정보
     * @param clock 현재 시간 제공자
     * @return 크레딧 사용 거래 엔티티
     */
    public static CreditTransaction spend(Credit credit, Clock clock) {
        return new CreditTransaction(
                UUID.randomUUID(),
                credit.userId(),
                CreditTransactionType.SPEND,
                credit.reason(),
                -credit.amount(),
                credit.idempotencyKey(),
                clock.now());
    }
}
