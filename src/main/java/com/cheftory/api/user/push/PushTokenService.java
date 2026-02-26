package com.cheftory.api.user.push;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.push.entity.PushToken;
import com.cheftory.api.user.push.entity.PushTokenPlatform;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.cheftory.api.user.push.exception.PushException;
import com.cheftory.api.user.push.repository.PushTokenRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 푸시 토큰 관리 서비스.
 *
 * <p>푸시 토큰 등록/비활성화와 알림 발송용 토큰 조회를 담당합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;
    private final Clock clock;

    /**
     * 푸시 토큰을 등록하거나 기존 토큰 소유자를 갱신합니다.
     *
     * @param userId 토큰 소유 유저 ID
     * @param provider 푸시 제공자
     * @param token 푸시 토큰 문자열
     * @param platform 디바이스 플랫폼
     */
    public void upsert(UUID userId, PushTokenProvider provider, String token, PushTokenPlatform platform) {
        pushTokenRepository.upsert(userId, provider, token, platform, clock);
    }

    /**
     * 알림 발송 대상 유저들의 활성 Expo 토큰 목록을 조회합니다.
     *
     * @param userIds 수신 대상 유저 ID 목록
     * @return 중복 제거된 Expo 토큰 목록
     */
    public List<String> findActiveExpoTokens(Collection<UUID> userIds) {
        return pushTokenRepository.findsActive(userIds).stream()
                .map(PushToken::getToken)
                .distinct()
                .toList();
    }

    /**
     * 푸시 토큰을 비활성화합니다.
     *
     * @param userId 토큰 소유 유저 ID
     * @param provider 푸시 제공자
     * @param token 비활성화할 푸시 토큰 문자열
     * @throws PushException 비활성화 처리 실패 시
     */
    public void delete(UUID userId, PushTokenProvider provider, String token) throws PushException {
        pushTokenRepository.deactivate(userId, provider, token, clock);
    }
}
