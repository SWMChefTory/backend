package com.cheftory.api._common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

/**
 * 현재 시간을 제공하는 클럭 클래스.
 *
 * <p>테스트 가능한 시간 제공을 위해 Seoul 시간대를 사용합니다.</p>
 */
@Component
public class Clock {

    /**
     * 현재 Seoul 시간을 반환합니다.
     *
     * @return 현재 Seoul 시간
     */
    public LocalDateTime now() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime seoulTime = now.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return seoulTime.toLocalDateTime();
    }

    /**
     * 현재 시간을 밀리초로 반환합니다.
     *
     * @return 에포크 타임 밀리초
     */
    public long nowMillis() {
        return Instant.now().toEpochMilli();
    }
}
