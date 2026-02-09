package com.cheftory.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.Login;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Login 도메인 테스트")
class LoginTest {

    private final UUID userId = UUID.randomUUID();
    private final Clock clock = new Clock();

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("Login 엔티티를 생성한다")
        void it_creates_login() {
            // given
            String refreshToken = "refresh-token-value";
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

            // when
            Login login = Login.create(userId, refreshToken, expiredAt, clock);

            // then
            assertThat(login.getId()).isNotNull();
            assertThat(login.getUserId()).isEqualTo(userId);
            assertThat(login.getRefreshToken()).isEqualTo(refreshToken);
            assertThat(login.getRefreshTokenExpiredAt()).isEqualTo(expiredAt);
            assertThat(login.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateRefreshToken 메서드는")
    class Describe_updateRefreshToken {

        @Test
        @DisplayName("리프레시 토큰과 만료 시간을 갱신한다")
        void it_updates_refresh_token() {
            // given
            String oldToken = "old-refresh-token";
            LocalDateTime oldExpiredAt = LocalDateTime.now().plusDays(7);
            Login login = Login.create(userId, oldToken, oldExpiredAt, clock);

            String newToken = "new-refresh-token";
            LocalDateTime newExpiredAt = LocalDateTime.now().plusDays(14);

            // when
            login.updateRefreshToken(newToken, newExpiredAt);

            // then
            assertThat(login.getRefreshToken()).isEqualTo(newToken);
            assertThat(login.getRefreshTokenExpiredAt()).isEqualTo(newExpiredAt);
        }
    }
}
