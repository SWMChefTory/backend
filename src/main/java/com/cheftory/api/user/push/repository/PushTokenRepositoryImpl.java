package com.cheftory.api.user.push.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.push.entity.PushToken;
import com.cheftory.api.user.push.entity.PushTokenPlatform;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.cheftory.api.user.push.entity.PushTokenStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * PushTokenRepository 구현체.
 */
@Repository
@RequiredArgsConstructor
public class PushTokenRepositoryImpl implements PushTokenRepository {

    private final PushTokenJpaRepository pushTokenJpaRepository;

    @Override
    public List<PushToken> findsActive(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return pushTokenJpaRepository.findAllByUserIdInAndStatus(userIds, PushTokenStatus.ACTIVE);
    }

    @Override
    public void upsert(UUID userId, PushTokenProvider provider, String token, PushTokenPlatform platform, Clock clock) {
        pushTokenJpaRepository
                .findByProviderAndToken(provider, token)
                .ifPresentOrElse(
                        pt -> {
                            pt.assignTo(userId, platform, clock);
                            pushTokenJpaRepository.save(pt);
                        },
                        () -> pushTokenJpaRepository.save(PushToken.create(userId, provider, token, platform, clock)));
    }

    @Override
    public void deactivate(UUID userId, PushTokenProvider provider, String token, Clock clock) {
        pushTokenJpaRepository
                .findByUserIdAndProviderAndToken(userId, provider, token)
                .ifPresent(pushToken -> {
                    pushToken.deactivate(clock);
                    pushTokenJpaRepository.save(pushToken);
                });
    }
}
