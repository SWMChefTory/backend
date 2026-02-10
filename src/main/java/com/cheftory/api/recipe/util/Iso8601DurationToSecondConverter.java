package com.cheftory.api.recipe.util;

import java.time.Duration;
import org.springframework.util.Assert;

/**
 * ISO 8601 기간 형식을 초 단위로 변환하는 유틸리티 클래스.
 */
public class Iso8601DurationToSecondConverter {
    /**
     * ISO 8601 기간 문자열을 초 단위로 변환합니다.
     *
     * @param iso8601 ISO 8601 기간 문자열 (예: "PT30S", "PT1H30M")
     * @return 초 단위 시간
     */
    public static Long convert(String iso8601) {
        Assert.notNull(iso8601, "null을 매개변수로 입력할 수 없습니다.");
        Duration duration = Duration.parse(iso8601);
        return duration.toSeconds();
    }
}
