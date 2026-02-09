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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(LoginRepositoryImpl.class)
class LoginRepositoryTest extends DbContextTest {

    @Autowired
    private LoginRepository loginRepository;

    @Test
    @DisplayName("로그인 생성")
    void create_login() {
        Clock clock = new Clock();
        UUID userId = UUID.randomUUID();
        String refreshToken = "refresh-token-value";
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

        Login login = Login.create(userId, refreshToken, expiredAt, clock);

        loginRepository.create(login);

        assertThat(login.getId()).isNotNull();
    }

    @Test
    @DisplayName("로그인 삭제 (로그아웃)")
    void delete_login() throws AuthException {
        Clock clock = new Clock();
        UUID userId = UUID.randomUUID();
        String refreshToken = "refresh-token-value";
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
        Login login = Login.create(userId, refreshToken, expiredAt, clock);
        loginRepository.create(login);

        loginRepository.delete(userId, refreshToken);

        assertThatThrownBy(() -> {
                    loginRepository.delete(userId, refreshToken);
                })
                .isInstanceOf(AuthException.class)
                .extracting("error")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("존재하지 않는 로그인 삭제 시도 시 예외 발생")
    void delete_byNotFoundLogin_throwException() {
        UUID userId = UUID.randomUUID();
        String refreshToken = "non-existent-token";

        assertThatThrownBy(() -> loginRepository.delete(userId, refreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("error")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("리프레시 토큰 갱신")
    void update_refreshToken() throws AuthException {
        Clock clock = new Clock();
        UUID userId = UUID.randomUUID();
        String oldToken = "old-refresh-token";
        LocalDateTime oldExpiredAt = LocalDateTime.now().plusDays(7);
        Login login = Login.create(userId, oldToken, oldExpiredAt, clock);
        loginRepository.create(login);

        String newToken = "new-refresh-token";
        LocalDateTime newExpiredAt = LocalDateTime.now().plusDays(14);

        loginRepository.update(userId, oldToken, newToken, newExpiredAt);

        assertThatThrownBy(() -> loginRepository.delete(userId, oldToken)).isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 갱신 시도 시 예외 발생")
    void update_byNotFoundRefreshToken_throwException() {
        UUID userId = UUID.randomUUID();
        String oldToken = "non-existent-token";
        String newToken = "new-refresh-token";
        LocalDateTime newExpiredAt = LocalDateTime.now().plusDays(14);

        assertThatThrownBy(() -> loginRepository.update(userId, oldToken, newToken, newExpiredAt))
                .isInstanceOf(AuthException.class)
                .extracting("error")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
