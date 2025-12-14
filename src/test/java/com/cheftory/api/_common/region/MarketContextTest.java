package com.cheftory.api._common.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.GlobalErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MarketContext Tests")
class MarketContextTest {

  @Test
  void required_shouldThrow_whenMissing() {
    assertThatThrownBy(MarketContext::required)
        .isInstanceOf(CheftoryException.class)
        .hasFieldOrPropertyWithValue("errorMessage", GlobalErrorCode.UNKNOWN_REGION);
  }

  @Test
  void with_shouldSet_andRestore() {
    assertThat(MarketContext.currentOrNull()).isNull();

    var info = new MarketContext.Info(Market.KOREA, "KR");
    try (var ignored = MarketContext.with(info)) {
      assertThat(MarketContext.required().market()).isEqualTo(Market.KOREA);
      assertThat(MarketContext.required().countryCode()).isEqualTo("KR");
    }

    assertThat(MarketContext.currentOrNull()).isNull();
  }

  @Test
  void threadLocal_shouldNotLeakAcrossThreads() throws Exception {
    try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
      Thread other =
          new Thread(
              () -> {
                assertThat(MarketContext.currentOrNull()).isNull();
              });

      other.start();
      other.join();

      assertThat(MarketContext.required().market()).isEqualTo(Market.KOREA);
    }

    assertThat(MarketContext.currentOrNull()).isNull();
  }
}
