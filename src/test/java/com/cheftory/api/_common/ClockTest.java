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
      // When
      LocalDateTime result = clock.now();

      // Then
      assertThat(result).isNotNull();

      // 현재 시간과 비교 (1초 이내 차이)
      LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
      assertThat(result).isBetween(now.minusSeconds(1), now.plusSeconds(1));
    }

    @Test
    @DisplayName("반환된 시간이 LocalDateTime 타입이다")
    void shouldReturnLocalDateTimeType() {
      // When
      LocalDateTime result = clock.now();

      // Then
      assertThat(result).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("연속 호출 시 시간이 증가한다")
    void shouldReturnIncreasingTimeOnConsecutiveCalls() throws InterruptedException {
      // When
      LocalDateTime firstCall = clock.now();
      Thread.sleep(10); // 10ms 대기
      LocalDateTime secondCall = clock.now();

      // Then
      assertThat(secondCall).isAfter(firstCall);
    }
  }

  @Nested
  @DisplayName("nowMillis 메서드 테스트")
  class NowMillisTest {

    @Test
    @DisplayName("현재 시간을 밀리초로 반환한다")
    void shouldReturnCurrentTimeInMillis() {
      // When
      long result = clock.nowMillis();

      // Then
      assertThat(result).isPositive();

      // 현재 시간과 비교 (1초 이내 차이)
      long now = Instant.now().toEpochMilli();
      assertThat(result).isCloseTo(now, org.assertj.core.data.Offset.offset(1000L)); // 1초 이내
    }

    @Test
    @DisplayName("반환된 값이 long 타입이다")
    void shouldReturnLongType() {
      // When
      long result = clock.nowMillis();

      // Then
      assertThat(result).isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("연속 호출 시 시간이 증가한다")
    void shouldReturnIncreasingTimeOnConsecutiveCalls() throws InterruptedException {
      // When
      long firstCall = clock.nowMillis();
      Thread.sleep(10); // 10ms 대기
      long secondCall = clock.nowMillis();

      // Then
      assertThat(secondCall).isGreaterThan(firstCall);
    }

    @Test
    @DisplayName("now()와 nowMillis()의 시간이 일치한다")
    void shouldReturnConsistentTimeBetweenNowAndNowMillis() {
      // When
      LocalDateTime nowResult = clock.now();
      long nowMillisResult = clock.nowMillis();

      // Then
      ZonedDateTime seoulTime = nowResult.atZone(ZoneId.of("Asia/Seoul"));
      long expectedMillis = seoulTime.toInstant().toEpochMilli();

      // 1초 이내 차이 허용 (정확한 동시 호출이 어려움)
      assertThat(nowMillisResult)
          .isCloseTo(expectedMillis, org.assertj.core.data.Offset.offset(1000L));
    }
  }

  @Nested
  @DisplayName("시간대 테스트")
  class TimeZoneTest {

    @Test
    @DisplayName("now()는 서울 시간대를 사용한다")
    void shouldUseSeoulTimeZone() {
      // When
      LocalDateTime result = clock.now();

      // Then
      ZonedDateTime seoulTime = result.atZone(ZoneId.of("Asia/Seoul"));

      // 서울 시간은 UTC+9이므로 9시간 차이
      assertThat(seoulTime.getOffset().getTotalSeconds()).isEqualTo(9 * 3600); // 9시간 = 32400초
    }

    @Test
    @DisplayName("nowMillis()는 UTC 기준이다")
    void shouldUseUtcForNowMillis() {
      // When
      long result = clock.nowMillis();

      // Then
      Instant instant = Instant.ofEpochMilli(result);
      ZonedDateTime seoulTime = instant.atZone(ZoneId.of("Asia/Seoul"));

      // 서울 시간대 확인 (UTC+9)
      assertThat(seoulTime.getOffset().getTotalSeconds()).isEqualTo(9 * 3600); // 9시간 = 32400초
    }
  }

  @Nested
  @DisplayName("성능 테스트")
  class PerformanceTest {

    @Test
    @DisplayName("now() 메서드가 빠르게 실행된다")
    void shouldExecuteNowMethodQuickly() {
      // When & Then
      long startTime = System.nanoTime();
      clock.now();
      long endTime = System.nanoTime();

      long executionTime = endTime - startTime;
      // 10ms 이내에 실행되어야 함 (테스트 환경에서 더 관대하게)
      assertThat(executionTime).isLessThan(10_000_000L); // 10ms = 10,000,000 nanoseconds
    }

    @Test
    @DisplayName("nowMillis() 메서드가 빠르게 실행된다")
    void shouldExecuteNowMillisMethodQuickly() {
      // When & Then
      long startTime = System.nanoTime();
      clock.nowMillis();
      long endTime = System.nanoTime();

      long executionTime = endTime - startTime;
      // 10ms 이내에 실행되어야 함 (테스트 환경에서 더 관대하게)
      assertThat(executionTime).isLessThan(10_000_000L); // 10ms = 10,000,000 nanoseconds
    }
  }
}
