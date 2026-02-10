package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.CreditTransaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 크레딧 거래 이력 JPA 리포지토리.
 */
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
