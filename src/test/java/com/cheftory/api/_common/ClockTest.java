package com.cheftory.api._common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Clock Tests")
class ClockTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = new Clock();
    }

    @Nested
    @DisplayName("now 메서드 테스트")
    class NowTest {

        @Test
        @DisplayName("현재 시간을 서울 시간으로 반환한다")
        void shouldReturnCurrentTimeInSeoulTimeZone() {
            LocalDateTime result = clock.now();

            assertThat(result).isNotNull();

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            assertThat(result).isBetween(now.minusSeconds(1), now.plusSeconds(1));
        }

        @Test
        @DisplayName("반환된 시간이 LocalDateTime 타입이다")
        void shouldReturnLocalDateTimeType() {
            LocalDateTime result = clock.now();

            assertThat(result).isInstanceOf(LocalDateTime.class);
        }

        @Test
        @DisplayName("연속 호출 시 시간이 증가한다")
        void shouldReturnIncreasingTimeOnConsecutiveCalls() throws InterruptedException {
            LocalDateTime firstCall = clock.now();
            Thread.sleep(10);
            LocalDateTime secondCall = clock.now();

            assertThat(secondCall).isAfter(firstCall);
        }
    }

    @Nested
    @DisplayName("nowMillis 메서드 테스트")
    class NowMillisTest {

        @Test
        @DisplayName("현재 시간을 밀리초로 반환한다")
        void shouldReturnCurrentTimeInMillis() {
            long result = clock.nowMillis();

            assertThat(result).isPositive();

            long now = Instant.now().toEpochMilli();
            assertThat(result).isCloseTo(now, org.assertj.core.data.Offset.offset(1000L));
        }

        @Test
        @DisplayName("반환된 값이 long 타입이다")
        void shouldReturnLongType() {
            long result = clock.nowMillis();

            assertThat(result).isInstanceOf(Long.class);
        }

        @Test
        @DisplayName("연속 호출 시 시간이 증가한다")
        void shouldReturnIncreasingTimeOnConsecutiveCalls() throws InterruptedException {
            long firstCall = clock.nowMillis();
            Thread.sleep(10);
            long secondCall = clock.nowMillis();

            assertThat(secondCall).isGreaterThan(firstCall);
        }

        @Test
        @DisplayName("now()와 nowMillis()의 시간이 일치한다")
        void shouldReturnConsistentTimeBetweenNowAndNowMillis() {
            LocalDateTime nowResult = clock.now();
            long nowMillisResult = clock.nowMillis();

            ZonedDateTime seoulTime = nowResult.atZone(ZoneId.of("Asia/Seoul"));
            long expectedMillis = seoulTime.toInstant().toEpochMilli();

            assertThat(nowMillisResult).isCloseTo(expectedMillis, org.assertj.core.data.Offset.offset(1000L));
        }
    }

    @Nested
    @DisplayName("시간대 테스트")
    class TimeZoneTest {

        @Test
        @DisplayName("now()는 서울 시간대를 사용한다")
        void shouldUseSeoulTimeZone() {
            LocalDateTime result = clock.now();

            ZonedDateTime seoulTime = result.atZone(ZoneId.of("Asia/Seoul"));

            assertThat(seoulTime.getOffset().getTotalSeconds()).isEqualTo(9 * 3600);
        }

        @Test
        @DisplayName("nowMillis()는 UTC 기준이다")
        void shouldUseUtcForNowMillis() {
            long result = clock.nowMillis();

            Instant instant = Instant.ofEpochMilli(result);
            ZonedDateTime seoulTime = instant.atZone(ZoneId.of("Asia/Seoul"));

            assertThat(seoulTime.getOffset().getTotalSeconds()).isEqualTo(9 * 3600);
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("now() 메서드가 빠르게 실행된다")
        void shouldExecuteNowMethodQuickly() {
            long startTime = System.nanoTime();
            clock.now();
            long endTime = System.nanoTime();

            long executionTime = endTime - startTime;
            assertThat(executionTime).isLessThan(10_000_000L);
        }

        @Test
        @DisplayName("nowMillis() 메서드가 빠르게 실행된다")
        void shouldExecuteNowMillisMethodQuickly() {
            long startTime = System.nanoTime();
            clock.nowMillis();
            long endTime = System.nanoTime();

            long executionTime = endTime - startTime;
            assertThat(executionTime).isLessThan(10_000_000L);
        }
    }
}
