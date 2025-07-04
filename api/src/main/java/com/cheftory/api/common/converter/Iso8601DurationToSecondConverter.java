package com.cheftory.api.common.converter;

import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.time.Duration;

public class Iso8601DurationToSecondConverter {
    public static Long convert(String iso8601){
        Assert.notNull(iso8601,"null을 매개변수로 입력할 수 없습니다.");
        Duration duration = Duration.parse(iso8601);
        return duration.toSeconds();
    }
}
