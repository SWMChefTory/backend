package com.cheftory.api.notification.entity;

/**
 * 알림 콘텐츠.
 *
 * @param title 제목
 * @param body 본문
 */
public record NotificationContent(String title, String body) {

    /**
     * 알림 콘텐츠를 생성합니다.
     */
    public static NotificationContent of(String title, String body) {
        return new NotificationContent(title, body);
    }
}
