package com.cheftory.api._common.region;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.GlobalErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketContextFilter extends OncePerRequestFilter {

  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain) {

    try {
      String headerValue = req.getHeader(MarketHeaders.COUNTRY_CODE);
      String countryCode = (headerValue != null) ? headerValue.trim() : null;

      if (countryCode == null || countryCode.isBlank() || countryCode.length() != 2) {
        throw new CheftoryException(GlobalErrorCode.UNKNOWN_REGION);
      }

      Market market = Market.fromCountryCode(countryCode);

      log.info(
          "method={}, uri={}, X-Country-Code={}",
          req.getMethod(),
          req.getRequestURI(),
          req.getHeader("X-Country-Code"));

      try (var ignored = MarketContext.with(new MarketContext.Info(market, countryCode))) {
        chain.doFilter(req, res);
      }
    } catch (Exception e) {
      handlerExceptionResolver.resolveException(req, res, null, e);
    }
  }
}
