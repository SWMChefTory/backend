package com.cheftory.api.credit;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.entity.CreditTransaction;
import com.cheftory.api.credit.entity.CreditUserBalance;
import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 크레딧 트랜잭션 서비스.
 * 크레딧 지급 및 사용 트랜잭션을 처리하고 멱등성을 보장합니다.
 */
@Service
@RequiredArgsConstructor
public class CreditTxService {

    private final CreditUserBalanceRepository balanceRepository;
    private final CreditTransactionRepository transactionRepository;
    private final Clock clock;

    /**
     * 크레딧 지급 트랜잭션을 실행합니다.
     *
     * @param credit 지급할 크레딧 정보
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    @Transactional
    public void grantTx(Credit credit) throws CreditException {
        CreditUserBalance balance = loadOrCreateBalance(credit.userId());

        if (alreadyProcessed(
                () -> transactionRepository.saveAndFlush(CreditTransaction.grant(credit, clock)),
                credit.idempotencyKey())) {
            return;
        }

        credit.grantTo(balance);
        balanceRepository.save(balance);
    }

    /**
     * 크레딧 사용 트랜잭션을 실행합니다.
     *
     * @param credit 사용할 크레딧 정보
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    @Transactional
    public void spendTx(Credit credit) throws CreditException {
        CreditUserBalance balance = loadOrCreateBalance(credit.userId());

        if (alreadyProcessed(
                () -> transactionRepository.saveAndFlush(CreditTransaction.spend(credit, clock)),
                credit.idempotencyKey())) {
            return;
        }

        credit.spendFrom(balance);
        balanceRepository.save(balance);
    }

    private boolean alreadyProcessed(Runnable saveTx, String idempotencyKey) {
        try {
            saveTx.run();
            return false;
        } catch (DataIntegrityViolationException e) {
            if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
                return true;
            }
            throw e;
        }
    }

    private CreditUserBalance loadOrCreateBalance(UUID userId) throws CreditException {
        var existing = balanceRepository.findById(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            return balanceRepository.saveAndFlush(CreditUserBalance.create(userId));
        } catch (DataIntegrityViolationException e) {
            return balanceRepository
                    .findById(userId)
                    .orElseThrow(() -> new CreditException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT));
        }
    }
}
