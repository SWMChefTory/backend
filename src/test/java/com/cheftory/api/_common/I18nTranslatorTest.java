package com.cheftory.api._common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

@DisplayName("I18nTranslator Tests")
class I18nTranslatorTest {

  @Test
  @DisplayName("KR 국가코드는 Locale.KOREA로 번역한다")
  void shouldTranslateWithKoreanLocale() {
    MessageSource messageSource = mock(MessageSource.class);
    I18nTranslator translator = new I18nTranslator(messageSource);

    String key = "recipe.cuisine.korean";
    when(messageSource.getMessage(eq(key), isNull(), eq(Locale.KOREA))).thenReturn("한식");

    try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
      String result = translator.translate(key);

      assertThat(result).isEqualTo("한식");
      verify(messageSource).getMessage(eq(key), isNull(), eq(Locale.KOREA));
    }
  }

  @Test
  @DisplayName("KR 이외의 국가코드는 Locale.ENGLISH로 번역한다")
  void shouldTranslateWithEnglishLocaleForNonKorean() {
    MessageSource messageSource = mock(MessageSource.class);
    I18nTranslator translator = new I18nTranslator(messageSource);

    String key = "recipe.cuisine.western";
    when(messageSource.getMessage(eq(key), isNull(), eq(Locale.ENGLISH))).thenReturn("Western");

    try (var ignored = MarketContext.with(new MarketContext.Info(Market.GLOBAL, "US"))) {
      String result = translator.translate(key);

      assertThat(result).isEqualTo("Western");
      verify(messageSource).getMessage(eq(key), isNull(), eq(Locale.ENGLISH));
    }
  }

  @Test
  @DisplayName("국가코드가 없으면 Locale.ENGLISH로 번역한다")
  void shouldTranslateWithEnglishLocaleWhenCountryCodeIsNull() {
    MessageSource messageSource = mock(MessageSource.class);
    I18nTranslator translator = new I18nTranslator(messageSource);

    String key = "recipe.cuisine.simple";
    when(messageSource.getMessage(eq(key), isNull(), eq(Locale.ENGLISH))).thenReturn("QuickMeals");

    try (var ignored = MarketContext.with(new MarketContext.Info(Market.GLOBAL, null))) {
      String result = translator.translate(key);

      assertThat(result).isEqualTo("QuickMeals");
      verify(messageSource).getMessage(eq(key), isNull(), eq(Locale.ENGLISH));
    }
  }
}
