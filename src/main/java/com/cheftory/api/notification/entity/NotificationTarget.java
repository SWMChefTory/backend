package com.cheftory.api.notification.entity;

import java.util.List;
import java.util.UUID;

/**
 * 알림 발송 대상.
 *
 * @param userIds 수신자 유저 ID 목록
 */
public record NotificationTarget(List<UUID> userIds) {

    /**
     * 수신자 목록을 불변 컬렉션으로 감싸 알림 대상을 생성합니다.
     */
    public static NotificationTarget of(List<UUID> userIds) {
        return new NotificationTarget(List.copyOf(userIds));
    }
}
