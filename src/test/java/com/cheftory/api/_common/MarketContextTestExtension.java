package com.cheftory.api._common;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MarketContextTestExtension implements BeforeEachCallback, AfterEachCallback {

  private static final ExtensionContext.Namespace NS =
      ExtensionContext.Namespace.create(MarketContextTestExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) {
    var scope = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"));
    context.getStore(NS).put("scope", scope);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    var scope = (AutoCloseable) context.getStore(NS).remove("scope");
    if (scope != null) {
      try {
        scope.close();
      } catch (Exception ignored) {
      }
    }
  }
}