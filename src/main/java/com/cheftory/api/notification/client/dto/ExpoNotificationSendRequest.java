package com.cheftory.api.notification.client.dto;

import com.cheftory.api.notification.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Expo Push 전송 요청 DTO.
 */
public record ExpoNotificationSendRequest(
        @JsonProperty("to") String to,
        @JsonProperty("title") String title,
        @JsonProperty("body") String body,
        @JsonProperty("sound") String sound,
        @JsonProperty("data") ExpoPushSendData data) {

    /**
     * 공통 알림 모델을 Expo 전송 요청 DTO로 변환합니다.
     *
     * @param expoToken Expo 푸시 토큰
     * @param notification 공통 알림 모델
     * @return Expo 전송 요청 DTO
     */
    public static ExpoNotificationSendRequest from(String expoToken, Notification notification) {
        return new ExpoNotificationSendRequest(
                expoToken,
                notification.content().title(),
                notification.content().body(),
                "default",
                ExpoPushSendData.from(notification));
    }

    public record ExpoPushSendData(
            @JsonProperty("action") String action,
            @JsonProperty("target_id") String targetId,
            @JsonProperty("type") String type,
            @JsonProperty("sent_at") String sentAt) {

        /**
         * 공통 알림 모델을 Expo data payload로 변환합니다.
         *
         * @param notification 공통 알림 모델
         * @return Expo data payload
         */
        public static ExpoPushSendData from(Notification notification) {
            return new ExpoPushSendData(
                    notification.type().name(),
                    notification.metadata().targetId(),
                    notification.metadata().targetType(),
                    notification.metadata().sentAt());
        }
    }
}
