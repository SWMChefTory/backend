package com.cheftory.api.credit.entity;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 크레딧 잔액을 나타내는 엔티티.
 * 낙관적 잠금을 사용하여 동시성을 제어합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table
public class CreditUserBalance {

    @Id
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private long balance;

    @Version
    @Column(nullable = false)
    private long version;

    /**
     * 새로운 사용자 크레딧 잔액을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 생성된 크레딧 잔액 엔티티
     * @throws CreditException 유효하지 않은 사용자인 경우
     */
    public static CreditUserBalance create(UUID userId) throws CreditException {
        if (userId == null) throw new CreditException(CreditErrorCode.CREDIT_INVALID_USER);
        return new CreditUserBalance(userId, 0L, 0L);
    }

    /**
     * 크레딧 잔액을 변경합니다.
     *
     * @param delta 변경할 크레딧 양 (양수: 증가, 음수: 감소)
     * @throws CreditException 크레딧이 부족한 경우
     */
    public void apply(long delta) throws CreditException {
        long next = this.balance + delta;
        if (next < 0) {
            throw new CreditException(CreditErrorCode.CREDIT_INSUFFICIENT);
        }
        this.balance = next;
    }
}
