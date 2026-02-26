package com.cheftory.api.user.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPushTokenAdapter 테스트")
class NotificationPushTokenAdapterTest {

    @Mock
    private PushTokenService pushTokenService;

    @InjectMocks
    private NotificationPushTokenAdapter adapter;

    @Nested
    @DisplayName("활성 Expo 토큰 조회 (findActiveExpoTokens)")
    class FindActiveExpoTokens {

        @Test
        @DisplayName("Then - PushTokenService 조회 결과를 그대로 반환한다")
        void thenDelegatesToPushTokenService() {
            List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<String> tokens = List.of("expo-token-1", "expo-token-2");
            when(pushTokenService.findActiveExpoTokens(userIds)).thenReturn(tokens);

            List<String> result = adapter.findActiveExpoTokens(userIds);

            assertThat(result).containsExactly("expo-token-1", "expo-token-2");
            verify(pushTokenService).findActiveExpoTokens(userIds);
        }
    }
}
