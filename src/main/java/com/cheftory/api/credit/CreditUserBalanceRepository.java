package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.CreditUserBalance;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 크레딧 잔액 JPA 리포지토리.
 */
public interface CreditUserBalanceRepository extends JpaRepository<CreditUserBalance, UUID> {}
