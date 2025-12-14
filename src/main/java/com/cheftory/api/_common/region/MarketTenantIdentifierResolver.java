package com.cheftory.api._common.region;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.GlobalErrorCode;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class MarketTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

  public static final String BOOTSTRAP_TENANT = "BOOTSTRAP";

  @Override
  public String resolveCurrentTenantIdentifier() {
    var info = MarketContext.currentOrNull();
    if (info != null && info.market() != null) {
      return info.market().name();
    }

    if (TransactionSynchronizationManager.isSynchronizationActive()
        || TransactionSynchronizationManager.isActualTransactionActive()) {
      throw new CheftoryException(GlobalErrorCode.UNKNOWN_REGION);
    }

    return BOOTSTRAP_TENANT;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}