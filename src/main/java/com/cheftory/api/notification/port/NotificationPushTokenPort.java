package com.cheftory.api.notification.port;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 알림 발송용 푸시 토큰 조회 포트.
 */
public interface NotificationPushTokenPort {

    /**
     * 알림 수신 대상 유저들의 활성 Expo 토큰 목록을 조회합니다.
     *
     * @param userIds 수신 대상 유저 ID 목록
     * @return 중복 제거된 활성 Expo 토큰 목록
     */
    List<String> findActiveExpoTokens(Collection<UUID> userIds);
}
