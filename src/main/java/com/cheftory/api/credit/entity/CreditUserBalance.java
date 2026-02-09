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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table
public class CreditUserBalance {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private long balance;

    @Version
    @Column(nullable = false)
    private long version;

    public static CreditUserBalance create(UUID userId) throws CreditException {
        if (userId == null) throw new CreditException(CreditErrorCode.CREDIT_INVALID_USER);
        return new CreditUserBalance(userId, 0L, 0L);
    }

    public void apply(long delta) throws CreditException {
        long next = this.balance + delta;
        if (next < 0) {
            throw new CreditException(CreditErrorCode.CREDIT_INSUFFICIENT);
        }
        this.balance = next;
    }
}
