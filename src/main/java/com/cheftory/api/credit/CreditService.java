package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.entity.CreditUserBalance;
import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final CreditTxService creditTxService;
    private final CreditUserBalanceRepository balanceRepository;

    public long getBalance(UUID userId) {
        return balanceRepository
                .findById(userId)
                .map(CreditUserBalance::getBalance)
                .orElse(0L);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5)
    public void grant(Credit credit) throws CreditException {
        creditTxService.grantTx(credit);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5)
    public void spend(Credit credit) throws CreditException {
        creditTxService.spendTx(credit);
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, Credit credit) throws CreditException {
        log.warn(
                "Credit optimistic lock retry exhausted. userId={}, reason={}, amount={}, key={}",
                credit.userId(),
                credit.reason(),
                credit.amount(),
                credit.idempotencyKey(),
                e);
        throw new CreditException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
    }
}
