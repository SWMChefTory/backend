package com.cheftory.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.Login;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Login 엔티티")
class LoginTest {

    private final UUID userId = UUID.randomUUID();
    private final Clock clock = new Clock();

    @Nested
    @DisplayName("로그인 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            String refreshToken;
            LocalDateTime expiredAt;

            @Test
            @DisplayName("Then - Login 엔티티를 생성한다")
            void thenCreatesLogin() {
                refreshToken = "refresh-token-value";
                expiredAt = LocalDateTime.now().plusDays(7);

                Login login = Login.create(userId, refreshToken, expiredAt, clock);

                assertThat(login.getId()).isNotNull();
                assertThat(login.getUserId()).isEqualTo(userId);
                assertThat(login.getRefreshToken()).isEqualTo(refreshToken);
                assertThat(login.getRefreshTokenExpiredAt()).isEqualTo(expiredAt);
                assertThat(login.getCreatedAt()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 갱신 (updateRefreshToken)")
    class UpdateRefreshToken {

        @Nested
        @DisplayName("Given - 기존 로그인이 있을 때")
        class GivenExistingLogin {
            Login login;
            String oldToken;
            LocalDateTime oldExpiredAt;

            @Test
            @DisplayName("Then - 리프레시 토큰과 만료 시간을 갱신한다")
            void thenUpdatesToken() {
                oldToken = "old-refresh-token";
                oldExpiredAt = LocalDateTime.now().plusDays(7);
                login = Login.create(userId, oldToken, oldExpiredAt, clock);

                String newToken = "new-refresh-token";
                LocalDateTime newExpiredAt = LocalDateTime.now().plusDays(14);

                login.updateRefreshToken(newToken, newExpiredAt);

                assertThat(login.getRefreshToken()).isEqualTo(newToken);
                assertThat(login.getRefreshTokenExpiredAt()).isEqualTo(newExpiredAt);
            }
        }
    }
}
