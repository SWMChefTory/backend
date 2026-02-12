package com.cheftory.api._common;

import com.cheftory.api._common.region.MarketContext;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * 국제화(i18n) 메시지 변환기.
 */
@Component
@RequiredArgsConstructor
public class I18nTranslator {
    /** 메시지 소스 */
    private final MessageSource messageSource;

    /**
     * 키에 해당하는 국제화된 메시지를 반환합니다.
     *
     * @param key 메시지 키
     * @return 국제화된 메시지
     */
    @SneakyThrows
    public String translate(String key) {
        String countryCode = MarketContext.required().countryCode();
        Locale locale = localeFromCountryCode(countryCode);
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * 국가 코드로부터 Locale을 생성합니다.
     *
     * @param countryCode ISO 3166-1 alpha-2 국가 코드
     * @return 해당하는 Locale 인스턴스
     */
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
