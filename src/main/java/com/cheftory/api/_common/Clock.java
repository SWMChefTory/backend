package com.cheftory.api._common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

@Component
public class Clock {

    public LocalDateTime now() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime seoulTime = now.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return seoulTime.toLocalDateTime();
    }

    public long nowMillis() {
        return Instant.now().toEpochMilli();
    }
}
