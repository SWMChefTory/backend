package com.cheftory.api.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.Login;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(LoginRepositoryImpl.class)
@DisplayName("LoginRepository 테스트")
class LoginRepositoryTest extends DbContextTest {

    @Autowired
    private LoginRepository loginRepository;

    @Nested
    @DisplayName("로그인 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 로그인 정보가 주어졌을 때")
        class GivenValidLogin {
            Login login;

            @Test
            @DisplayName("Then - 로그인을 생성한다")
            void thenCreatesLogin() {
                Clock clock = new Clock();
                UUID userId = UUID.randomUUID();
                String refreshToken = "refresh-token-value";
                LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

                login = Login.create(userId, refreshToken, expiredAt, clock);
                loginRepository.create(login);

                assertThat(login.getId()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("로그인 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 존재하는 로그인이 있을 때")
        class GivenExistingLogin {
            UUID userId;
            String refreshToken;

            @Test
            @DisplayName("Then - 로그인을 삭제한다")
            void thenDeletesLogin() throws AuthException {
                Clock clock = new Clock();
                userId = UUID.randomUUID();
                refreshToken = "refresh-token-value";
                LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
                Login login = Login.create(userId, refreshToken, expiredAt, clock);
                loginRepository.create(login);

                loginRepository.delete(userId, refreshToken);

                assertThatThrownBy(() -> loginRepository.delete(userId, refreshToken))
                        .isInstanceOf(AuthException.class)
                        .extracting("error")
                        .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 로그인일 때")
        class GivenNonExistingLogin {
            UUID userId;
            String refreshToken;

            @Test
            @DisplayName("Then - INVALID_REFRESH_TOKEN 예외를 던진다")
            void thenThrowsException() {
                userId = UUID.randomUUID();
                refreshToken = "non-existent-token";

                assertThatThrownBy(() -> loginRepository.delete(userId, refreshToken))
                        .isInstanceOf(AuthException.class)
                        .extracting("error")
                        .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 갱신 (update)")
    class Update {

        @Nested
        @DisplayName("Given - 존재하는 리프레시 토큰일 때")
        class GivenExistingToken {
            UUID userId;
            String oldToken;
            String newToken;
            LocalDateTime newExpiredAt;

            @Test
            @DisplayName("Then - 토큰을 갱신한다")
            void thenUpdatesToken() throws AuthException {
                Clock clock = new Clock();
                userId = UUID.randomUUID();
                oldToken = "old-refresh-token";
                LocalDateTime oldExpiredAt = LocalDateTime.now().plusDays(7);
                Login login = Login.create(userId, oldToken, oldExpiredAt, clock);
                loginRepository.create(login);

                newToken = "new-refresh-token";
                newExpiredAt = LocalDateTime.now().plusDays(14);

                loginRepository.update(userId, oldToken, newToken, newExpiredAt);

                assertThatThrownBy(() -> loginRepository.delete(userId, oldToken))
                        .isInstanceOf(AuthException.class);
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 리프레시 토큰일 때")
        class GivenNonExistingToken {
            UUID userId;
            String oldToken;
            String newToken;
            LocalDateTime newExpiredAt;

            @Test
            @DisplayName("Then - INVALID_REFRESH_TOKEN 예외를 던진다")
            void thenThrowsException() {
                userId = UUID.randomUUID();
                oldToken = "non-existent-token";
                newToken = "new-refresh-token";
                newExpiredAt = LocalDateTime.now().plusDays(14);

                assertThatThrownBy(() -> loginRepository.update(userId, oldToken, newToken, newExpiredAt))
                        .isInstanceOf(AuthException.class)
                        .extracting("error")
                        .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
        }
    }
}
