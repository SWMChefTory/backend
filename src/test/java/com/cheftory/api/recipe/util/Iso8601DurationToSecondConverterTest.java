package com.cheftory.api.recipe.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Iso8601DurationToSecondConverter 테스트")
class Iso8601DurationToSecondConverterTest {

    @Nested
    @DisplayName("convert 메서드")
    class Convert {

        @Test
        @DisplayName("PT30S를 30초로 변환한다")
        void converts30Seconds() {
            Long result = Iso8601DurationToSecondConverter.convert("PT30S");
            assertThat(result).isEqualTo(30L);
        }

        @Test
        @DisplayName("PT1M을 60초로 변환한다")
        void converts1Minute() {
            Long result = Iso8601DurationToSecondConverter.convert("PT1M");
            assertThat(result).isEqualTo(60L);
        }

        @Test
        @DisplayName("PT1H을 3600초로 변환한다")
        void converts1Hour() {
            Long result = Iso8601DurationToSecondConverter.convert("PT1H");
            assertThat(result).isEqualTo(3600L);
        }

        @Test
        @DisplayName("PT1H30M을 5400초로 변환한다")
        void converts1Hour30Minutes() {
            Long result = Iso8601DurationToSecondConverter.convert("PT1H30M");
            assertThat(result).isEqualTo(5400L);
        }

        @Test
        @DisplayName("PT10M30S를 630초로 변환한다")
        void converts10Minutes30Seconds() {
            Long result = Iso8601DurationToSecondConverter.convert("PT10M30S");
            assertThat(result).isEqualTo(630L);
        }

        @Test
        @DisplayName("null 입력 시 예외를 던진다")
        void throwsExceptionForNull() {
            assertThatThrownBy(() -> Iso8601DurationToSecondConverter.convert(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("잘못된 형식 입력 시 예외를 던진다")
        void throwsExceptionForInvalidFormat() {
            assertThatThrownBy(() -> Iso8601DurationToSecondConverter.convert("invalid"))
                    .isInstanceOf(Exception.class);
        }
    }
}
