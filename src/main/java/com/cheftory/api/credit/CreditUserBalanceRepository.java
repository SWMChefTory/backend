package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.CreditUserBalance;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditUserBalanceRepository extends JpaRepository<CreditUserBalance, UUID> {}
