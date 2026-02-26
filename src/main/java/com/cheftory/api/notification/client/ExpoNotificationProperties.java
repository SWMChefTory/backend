package com.cheftory.api.notification.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Expo Push API 연동 설정 프로퍼티.
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "expo.notification")
public class ExpoNotificationProperties {
    /**
     * Expo 전송 활성화 여부.
     */
    private boolean enabled;
    /**
     * Expo Push 전송 API base URL.
     */
    private String sendUrl;
}
