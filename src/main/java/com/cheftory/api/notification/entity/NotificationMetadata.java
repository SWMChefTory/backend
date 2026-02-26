package com.cheftory.api.notification.entity;

/**
 * 알림 메타데이터.
 *
 * @param targetId 대상 리소스 ID
 * @param targetType 대상 리소스 타입
 * @param sentAt 발송 시각 문자열
 */
public record NotificationMetadata(String targetId, String targetType, String sentAt) {

    /**
     * 알림 메타데이터를 생성합니다.
     */
    public static NotificationMetadata of(String targetId, String targetType, String sentAt) {
        return new NotificationMetadata(targetId, targetType, sentAt);
    }
}
