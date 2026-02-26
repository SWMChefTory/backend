package com.cheftory.api.notification.entity;

/**
 * 알림 도메인 모델.
 *
 * @param type 알림 유형
 * @param content 알림 콘텐츠
 * @param target 알림 대상
 * @param metadata 채널 변환에 사용할 메타데이터
 */
public record Notification(
        NotificationType type, NotificationContent content, NotificationTarget target, NotificationMetadata metadata) {

    /**
     * 알림 도메인 모델을 생성합니다.
     */
    public static Notification of(
            NotificationType type,
            NotificationContent content,
            NotificationTarget target,
            NotificationMetadata metadata) {
        return new Notification(type, content, target, metadata);
    }
}
