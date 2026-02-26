package com.cheftory.api.user.push.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PushToken 엔티티 테스트")
class PushTokenTest {

    private final Clock clock = mock(Clock.class);

    @Nested
    @DisplayName("생성 (create)")
    class Create {

        @Test
        @DisplayName("Then - Clock.now() 기준으로 활성 토큰을 생성한다")
        void thenCreatesActiveTokenUsingClock() {
            LocalDateTime now = LocalDateTime.of(2026, 2, 26, 12, 0, 0);
            when(clock.now()).thenReturn(now);

            UUID userId = UUID.randomUUID();
            PushToken pushToken =
                    PushToken.create(userId, PushTokenProvider.EXPO, "expo-token", PushTokenPlatform.IOS, clock);

            assertThat(pushToken.getId()).isNotNull();
            assertThat(pushToken.getUserId()).isEqualTo(userId);
            assertThat(pushToken.getProvider()).isEqualTo(PushTokenProvider.EXPO);
            assertThat(pushToken.getToken()).isEqualTo("expo-token");
            assertThat(pushToken.getPlatform()).isEqualTo(PushTokenPlatform.IOS);
            assertThat(pushToken.getStatus()).isEqualTo(PushTokenStatus.ACTIVE);
            assertThat(pushToken.getCreatedAt()).isEqualTo(now);
            assertThat(pushToken.getUpdatedAt()).isEqualTo(now);
            assertThat(pushToken.getLastSeenAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("재할당 (assignTo)")
    class AssignTo {

        @Test
        @DisplayName("Then - 사용자/플랫폼/상태/시간을 갱신한다")
        void thenReassignsAndUpdatesTimestamps() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 2, 26, 12, 0, 0);
            LocalDateTime reassignedAt = LocalDateTime.of(2026, 2, 26, 12, 5, 0);
            when(clock.now()).thenReturn(createdAt, reassignedAt);

            PushToken pushToken = PushToken.create(
                    UUID.randomUUID(), PushTokenProvider.EXPO, "expo-token", PushTokenPlatform.IOS, clock);

            UUID newUserId = UUID.randomUUID();
            pushToken.assignTo(newUserId, PushTokenPlatform.ANDROID, clock);

            assertThat(pushToken.getUserId()).isEqualTo(newUserId);
            assertThat(pushToken.getPlatform()).isEqualTo(PushTokenPlatform.ANDROID);
            assertThat(pushToken.getStatus()).isEqualTo(PushTokenStatus.ACTIVE);
            assertThat(pushToken.getCreatedAt()).isEqualTo(createdAt);
            assertThat(pushToken.getUpdatedAt()).isEqualTo(reassignedAt);
            assertThat(pushToken.getLastSeenAt()).isEqualTo(reassignedAt);
        }
    }

    @Nested
    @DisplayName("비활성화 (deactivate)")
    class Deactivate {

        @Test
        @DisplayName("Then - 상태를 INACTIVE로 변경하고 updatedAt을 갱신한다")
        void thenMarksInactiveAndUpdatesTimestamp() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 2, 26, 12, 0, 0);
            LocalDateTime deactivatedAt = LocalDateTime.of(2026, 2, 26, 12, 10, 0);
            when(clock.now()).thenReturn(createdAt, deactivatedAt);

            PushToken pushToken = PushToken.create(
                    UUID.randomUUID(), PushTokenProvider.EXPO, "expo-token", PushTokenPlatform.IOS, clock);

            pushToken.deactivate(clock);

            assertThat(pushToken.getStatus()).isEqualTo(PushTokenStatus.INACTIVE);
            assertThat(pushToken.getCreatedAt()).isEqualTo(createdAt);
            assertThat(pushToken.getUpdatedAt()).isEqualTo(deactivatedAt);
            assertThat(pushToken.getLastSeenAt()).isEqualTo(createdAt);
        }
    }
}
