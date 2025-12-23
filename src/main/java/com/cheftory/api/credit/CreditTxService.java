package com.cheftory.api.credit;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditTxService {

  private final CreditUserBalanceRepository balanceRepository;
  private final CreditTransactionRepository transactionRepository;
  private final Clock clock;

  @Transactional
  public void grantTx(Credit credit) {
    var info = MarketContext.required();
    CreditUserBalance balance = loadOrCreateBalance(credit.userId());

    if (alreadyProcessed(
        () ->
            transactionRepository.save(
                CreditTransaction.grant(credit, info.market(), info.countryCode(), clock)),
        credit.idempotencyKey())) {
      return;
    }

    credit.grantTo(balance);
    balanceRepository.save(balance);
  }

  @Transactional
  public void spendTx(Credit credit) {
    var info = MarketContext.required();
    CreditUserBalance balance = loadOrCreateBalance(credit.userId());

    if (alreadyProcessed(
        () ->
            transactionRepository.save(
                CreditTransaction.spend(credit, info.market(), info.countryCode(), clock)),
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

  private CreditUserBalance loadOrCreateBalance(UUID userId) {
    return balanceRepository
        .findById(userId)
        .orElseGet(
            () -> {
              try {
                return balanceRepository.saveAndFlush(CreditUserBalance.create(userId));
              } catch (DataIntegrityViolationException e) {
                return balanceRepository.findById(userId).orElseThrow();
              }
            });
  }
}
