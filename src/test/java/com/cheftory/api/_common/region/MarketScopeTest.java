package com.cheftory.api._common.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MarketScope Tests")
class MarketScopeTest {

  private static class TestEntity extends MarketScope {}

  @Test
  void onCreate_shouldSetCountryCode() throws Exception {
    try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
      TestEntity e = new TestEntity();

      Method m = MarketScope.class.getDeclaredMethod("onCreate");
      m.setAccessible(true);
      m.invoke(e);

      assertThat(e.getCountryCode()).isEqualTo("KR");
    }
  }

  @Test
  void onCreate_shouldThrow_whenNoContext() throws Exception {
    TestEntity e = new TestEntity();

    Method m = MarketScope.class.getDeclaredMethod("onCreate");
    m.setAccessible(true);

    assertThatThrownBy(() -> m.invoke(e))
        .hasCauseInstanceOf(com.cheftory.api.exception.CheftoryException.class);
  }
}
