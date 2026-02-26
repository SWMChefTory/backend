package com.cheftory.api.user.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.push.entity.PushToken;
import com.cheftory.api.user.push.entity.PushTokenPlatform;
import com.cheftory.api.user.push.entity.PushTokenProvider;
import com.cheftory.api.user.push.exception.PushException;
import com.cheftory.api.user.push.repository.PushTokenRepository;
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
@DisplayName("PushTokenService 테스트")
class PushTokenServiceTest {

    @Mock
    private PushTokenRepository pushTokenRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private PushTokenService pushTokenService;

    @Nested
    @DisplayName("활성 Expo 토큰 조회 (findActiveExpoTokens)")
    class FindActiveExpoTokens {

        @Nested
        @DisplayName("Given - 중복 토큰이 포함되어 있을 때")
        class GivenDuplicateTokens {

            @Test
            @DisplayName("Then - 중복 제거된 토큰 목록을 반환한다")
            void thenReturnsDistinctTokens() {
                UUID userId = UUID.randomUUID();
                PushToken token1 = mock(PushToken.class);
                PushToken token2 = mock(PushToken.class);
                PushToken token3 = mock(PushToken.class);

                when(token1.getToken()).thenReturn("expo-token-1");
                when(token2.getToken()).thenReturn("expo-token-1");
                when(token3.getToken()).thenReturn("expo-token-2");
                when(pushTokenRepository.findsActive(List.of(userId))).thenReturn(List.of(token1, token2, token3));

                List<String> result = pushTokenService.findActiveExpoTokens(List.of(userId));

                assertThat(result).containsExactly("expo-token-1", "expo-token-2");
            }
        }
    }

    @Nested
    @DisplayName("푸시 토큰 저장 (upsert)")
    class Upsert {

        @Test
        @DisplayName("Then - repository.upsert를 호출한다")
        void thenDelegatesToRepository() {
            UUID userId = UUID.randomUUID();

            pushTokenService.upsert(userId, PushTokenProvider.EXPO, "expo-token", PushTokenPlatform.ANDROID);

            verify(pushTokenRepository)
                    .upsert(userId, PushTokenProvider.EXPO, "expo-token", PushTokenPlatform.ANDROID, clock);
        }
    }

    @Nested
    @DisplayName("푸시 토큰 삭제 (delete)")
    class Delete {

        @Test
        @DisplayName("Then - repository.deactivate를 호출한다")
        void thenDelegatesToRepository() throws PushException {
            UUID userId = UUID.randomUUID();

            pushTokenService.delete(userId, PushTokenProvider.EXPO, "expo-token");

            verify(pushTokenRepository).deactivate(userId, PushTokenProvider.EXPO, "expo-token", clock);
        }
    }
}
