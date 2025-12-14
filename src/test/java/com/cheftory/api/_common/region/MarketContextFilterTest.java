package com.cheftory.api._common.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

@DisplayName("MarketContextFilter Tests")
class MarketContextFilterTest {

  private final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
  private final MarketContextFilter filter = new MarketContextFilter(resolver);

  @Test
  void shouldInitializeContext_inChain_andClearAfter() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    req.addHeader(MarketHeaders.COUNTRY_CODE, "KR");

    FilterChain chain =
        (request, response) -> {
          var info = MarketContext.required();
          assertThat(info.market()).isEqualTo(Market.KOREA);
          assertThat(info.countryCode()).isEqualTo("KR");
        };

    filter.doFilter(req, res, chain);

    assertThat(MarketContext.currentOrNull()).isNull();
  }

  @Test
  void shouldSetGlobalMarket_whenNonKr() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    MockHttpServletResponse res = new MockHttpServletResponse();
    req.addHeader(MarketHeaders.COUNTRY_CODE, "US");

    FilterChain chain =
        (request, response) -> {
          var info = MarketContext.required();
          assertThat(info.market()).isEqualTo(Market.GLOBAL);
          assertThat(info.countryCode()).isEqualTo("US");
        };

    filter.doFilter(req, res, chain);

    assertThat(MarketContext.currentOrNull()).isNull();
  }
}
