package com.cheftory.api._common;

import com.cheftory.api._common.region.MarketContext;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class I18nTranslator {
  private final MessageSource messageSource;

  public String translate(String key) {
    String countryCode = MarketContext.required().countryCode();
    Locale locale = localeFromCountryCode(countryCode);
    return messageSource.getMessage(key, null, locale);
  }

  private Locale localeFromCountryCode(String countryCode) {
    if (countryCode == null) {
      return Locale.ENGLISH;
    }
    return switch (countryCode.toUpperCase()) {
      case "KR" -> Locale.KOREA;
      default -> Locale.ENGLISH;
    };
  }

}
