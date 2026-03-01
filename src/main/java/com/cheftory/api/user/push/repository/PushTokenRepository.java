package com.cheftory.api.user.push.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.push.entity.PushToken;
import com.cheftory.api.user.push.entity.PushTokenPlatform;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 푸시 토큰 리포지토리 인터페이스.
 */
public interface PushTokenRepository {

    List<PushToken> findsActive(Collection<UUID> userIds);

    void upsert(UUID userId, PushTokenProvider provider, String token, PushTokenPlatform platform, Clock clock);

    void deactivate(UUID userId, PushTokenProvider provider, String token, Clock clock);
}
