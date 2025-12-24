package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.entity.CreditUserBalance;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditService {

  private final CreditTxService creditTxService;
  private final CreditUserBalanceRepository balanceRepository;

  public long getBalance(UUID userId) {
    return balanceRepository.findById(userId).map(CreditUserBalance::getBalance).orElse(0L);
  }

  @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5)
  public void grant(Credit credit) {
    creditTxService.grantTx(credit);
  }

  @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5)
  public void spend(Credit credit) {
    creditTxService.spendTx(credit);
  }
}
