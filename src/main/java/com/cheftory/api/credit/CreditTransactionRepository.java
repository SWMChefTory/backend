package com.cheftory.api.credit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
  boolean existsByIdempotencyKey(String idempotencyKey);
}
