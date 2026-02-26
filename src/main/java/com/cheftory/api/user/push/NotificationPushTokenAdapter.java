package com.cheftory.api.user.push;

import com.cheftory.api.notification.port.NotificationPushTokenPort;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * notification 도메인에서 사용하는 푸시 토큰 조회 어댑터.
 */
@Component
@RequiredArgsConstructor
public class NotificationPushTokenAdapter implements NotificationPushTokenPort {

    private final PushTokenService pushTokenService;

    /**
     * notification 도메인의 토큰 조회 요청을 push 토큰 서비스로 위임합니다.
     */
    @Override
    public List<String> findActiveExpoTokens(Collection<UUID> userIds) {
        return pushTokenService.findActiveExpoTokens(userIds);
    }
}
