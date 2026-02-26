package com.cheftory.api.user.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.user.push.entity.PushToken;
import com.cheftory.api.user.push.entity.PushTokenPlatform;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.cheftory.api.user.push.entity.PushTokenStatus;
import com.cheftory.api.user.push.repository.PushTokenRepository;
import com.cheftory.api.user.push.repository.PushTokenRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(PushTokenRepositoryImpl.class)
@DisplayName("PushTokenRepository 테스트")
class PushTokenRepositoryTest extends DbContextTest {

    @Autowired
    private PushTokenRepository pushTokenRepository;

    private final LocalDateTime createdAt = LocalDateTime.of(2026, 2, 26, 10, 0, 0);
    private final LocalDateTime updatedAt = LocalDateTime.of(2026, 2, 26, 10, 5, 0);
    private final LocalDateTime deactivatedAt = LocalDateTime.of(2026, 2, 26, 10, 10, 0);

    @Nested
    @DisplayName("활성 토큰 조회 (findsActive)")
    class FindsActive {

        @Nested
        @DisplayName("Given - 활성/비활성 토큰이 섞여 있을 때")
        class GivenMixedTokens {
            UUID targetUserId;
            UUID otherUserId;

            @BeforeEach
            void setUp() {
                targetUserId = UUID.randomUUID();
                otherUserId = UUID.randomUUID();

                pushTokenRepository.upsert(
                        targetUserId,
                        PushTokenProvider.EXPO,
                        uniqueToken("active-target"),
                        PushTokenPlatform.IOS,
                        clockWith(createdAt));

                String inactiveToken = uniqueToken("inactive-target");
                pushTokenRepository.upsert(
                        targetUserId,
                        PushTokenProvider.EXPO,
                        inactiveToken,
                        PushTokenPlatform.ANDROID,
                        clockWith(createdAt));
                pushTokenRepository.deactivate(
                        targetUserId, PushTokenProvider.EXPO, inactiveToken, clockWith(deactivatedAt));

                pushTokenRepository.upsert(
                        otherUserId,
                        PushTokenProvider.EXPO,
                        uniqueToken("active-other"),
                        PushTokenPlatform.IOS,
                        clockWith(createdAt));
            }

            @Nested
            @DisplayName("When - 대상 유저들로 조회하면")
            class WhenFinding {
                List<PushToken> results;

                @BeforeEach
                void setUp() {
                    results = pushTokenRepository.findsActive(List.of(targetUserId));
                }

                @Test
                @DisplayName("Then - 대상 유저의 ACTIVE 토큰만 반환한다")
                void thenReturnsOnlyActiveTokensOfTargetUsers() {
                    assertThat(results).hasSize(1);
                    assertThat(results.getFirst().getUserId()).isEqualTo(targetUserId);
                    assertThat(results.getFirst().getStatus()).isEqualTo(PushTokenStatus.ACTIVE);
                }
            }
        }

        @Nested
        @DisplayName("Given - userIds가 비어있을 때")
        class GivenEmptyUserIds {

            @Test
            @DisplayName("Then - 빈 목록을 반환한다")
            void thenReturnsEmptyList() {
                assertThat(pushTokenRepository.findsActive(List.of())).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("토큰 등록/재할당 (upsert)")
    class Upsert {

        @Nested
        @DisplayName("Given - 동일 provider/token이 존재하지 않을 때")
        class GivenNewToken {

            @Nested
            @DisplayName("When - upsert를 호출하면")
            class WhenUpserting {
                UUID userId;
                String token;

                @BeforeEach
                void setUp() {
                    userId = UUID.randomUUID();
                    token = uniqueToken("new");
                    pushTokenRepository.upsert(
                            userId, PushTokenProvider.EXPO, token, PushTokenPlatform.IOS, clockWith(createdAt));
                }

                @Test
                @DisplayName("Then - ACTIVE 상태의 토큰이 생성된다")
                void thenCreatesPushToken() {
                    List<PushToken> results = pushTokenRepository.findsActive(List.of(userId));

                    assertThat(results).hasSize(1);
                    PushToken saved = results.getFirst();
                    assertThat(saved.getUserId()).isEqualTo(userId);
                    assertThat(saved.getProvider()).isEqualTo(PushTokenProvider.EXPO);
                    assertThat(saved.getToken()).isEqualTo(token);
                    assertThat(saved.getPlatform()).isEqualTo(PushTokenPlatform.IOS);
                    assertThat(saved.getStatus()).isEqualTo(PushTokenStatus.ACTIVE);
                    assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
                    assertThat(saved.getUpdatedAt()).isEqualTo(createdAt);
                    assertThat(saved.getLastSeenAt()).isEqualTo(createdAt);
                }
            }
        }

        @Nested
        @DisplayName("Given - 동일 provider/token이 이미 존재할 때")
        class GivenExistingToken {
            UUID oldUserId;
            UUID newUserId;
            String token;
            UUID existingId;

            @BeforeEach
            void setUp() {
                oldUserId = UUID.randomUUID();
                newUserId = UUID.randomUUID();
                token = uniqueToken("existing");

                pushTokenRepository.upsert(
                        oldUserId, PushTokenProvider.EXPO, token, PushTokenPlatform.IOS, clockWith(createdAt));
                existingId = pushTokenRepository
                        .findsActive(List.of(oldUserId))
                        .getFirst()
                        .getId();

                pushTokenRepository.upsert(
                        newUserId, PushTokenProvider.EXPO, token, PushTokenPlatform.ANDROID, clockWith(updatedAt));
            }

            @Nested
            @DisplayName("When - 다시 upsert를 호출하면")
            class WhenReUpserting {

                @Test
                @DisplayName("Then - 기존 토큰을 재할당하고 ACTIVE로 유지한다")
                void thenReassignsExistingToken() {
                    List<PushToken> oldUserTokens = pushTokenRepository.findsActive(List.of(oldUserId));
                    List<PushToken> newUserTokens = pushTokenRepository.findsActive(List.of(newUserId));

                    assertThat(oldUserTokens).isEmpty();
                    assertThat(newUserTokens).hasSize(1);

                    PushToken saved = newUserTokens.getFirst();
                    assertThat(saved.getId()).isEqualTo(existingId);
                    assertThat(saved.getUserId()).isEqualTo(newUserId);
                    assertThat(saved.getToken()).isEqualTo(token);
                    assertThat(saved.getPlatform()).isEqualTo(PushTokenPlatform.ANDROID);
                    assertThat(saved.getStatus()).isEqualTo(PushTokenStatus.ACTIVE);
                    assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
                    assertThat(saved.getUpdatedAt()).isEqualTo(updatedAt);
                    assertThat(saved.getLastSeenAt()).isEqualTo(updatedAt);
                }
            }
        }
    }

    @Nested
    @DisplayName("토큰 비활성화 (deactivate)")
    class Deactivate {

        @Nested
        @DisplayName("Given - 해당 유저의 활성 토큰이 존재할 때")
        class GivenExistingToken {
            UUID userId;
            String token;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                token = uniqueToken("deactivate");

                pushTokenRepository.upsert(
                        userId, PushTokenProvider.EXPO, token, PushTokenPlatform.IOS, clockWith(createdAt));
                pushTokenRepository.deactivate(userId, PushTokenProvider.EXPO, token, clockWith(deactivatedAt));
            }

            @Nested
            @DisplayName("When - deactivate를 호출하면")
            class WhenDeactivating {

                @Test
                @DisplayName("Then - 상태가 INACTIVE로 변경된다")
                void thenMarksInactive() {
                    assertThat(pushTokenRepository.findsActive(List.of(userId))).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - 토큰이 존재하지 않을 때")
        class GivenMissingToken {

            @Test
            @DisplayName("Then - 예외 없이 종료한다")
            void thenDoesNothing() {
                UUID userId = UUID.randomUUID();
                String token = uniqueToken("exists");
                pushTokenRepository.upsert(
                        userId, PushTokenProvider.EXPO, token, PushTokenPlatform.IOS, clockWith(createdAt));

                pushTokenRepository.deactivate(
                        UUID.randomUUID(), PushTokenProvider.EXPO, uniqueToken("missing"), clockWith(deactivatedAt));

                assertThat(pushTokenRepository.findsActive(List.of(userId))).hasSize(1);
            }
        }
    }

    private static Clock clockWith(LocalDateTime now) {
        Clock clock = mock(Clock.class);
        doReturn(now).when(clock).now();
        return clock;
    }

    private static String uniqueToken(String suffix) {
        return "ExponentPushToken[" + suffix + "-" + UUID.randomUUID() + "]";
    }
}
