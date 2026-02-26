package com.cheftory.api.user.push.repository;

import com.cheftory.api.user.push.entity.PushToken;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.cheftory.api.user.push.entity.PushTokenStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushTokenJpaRepository extends JpaRepository<PushToken, UUID> {
    Optional<PushToken> findByProviderAndToken(PushTokenProvider provider, String token);

    Optional<PushToken> findByUserIdAndProviderAndToken(UUID userId, PushTokenProvider provider, String token);

    List<PushToken> findAllByUserIdInAndStatus(Collection<UUID> userIds, PushTokenStatus status);
}
