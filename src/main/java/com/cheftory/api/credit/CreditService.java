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

/**
 * 크레딧 서비스.
 * 크레딧 조회, 지급, 사용 기능을 제공하며 낙관적 잠금 재시도를 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final CreditTxService txService;
    private final CreditUserBalanceRepository repository;

    /**
     * 사용자의 크레딧 잔액을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 크레딧 잔액
     */
    public long getBalance(UUID userId) {
        return repository.findById(userId).map(CreditUserBalance::getBalance).orElse(0L);
    }

    /**
     * 크레딧을 지급합니다.
     * 낙관적 잠금 실패 시 최대 5회 재시도합니다.
     *
     * @param credit 지급할 크레딧 정보
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5)
    public void grant(Credit credit) throws CreditException {
        txService.grantTx(credit);
    }

    /**
     * 크레딧을 사용합니다.
     * 낙관적 잠금 실패 시 최대 5회 재시도합니다.
     *
     * @param credit 사용할 크레딧 정보
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5)
    public void spend(Credit credit) throws CreditException {
        txService.spendTx(credit);
    }

    /**
     * 낙관적 잠금 재시도 실패 시 복구 처리를 수행합니다.
     *
     * @param e 낙관적 잠금 실패 예외
     * @param credit 처리 중이던 크레딧 정보
     * @throws CreditException 크레딧 동시성 충돌 예외
     */
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
