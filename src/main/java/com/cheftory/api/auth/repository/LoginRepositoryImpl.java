package com.cheftory.api.auth.repository;

import com.cheftory.api.auth.entity.Login;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * LoginRepository 구현체
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class LoginRepositoryImpl implements LoginRepository {
    private final LoginJpaRepository loginJpaRepository;

    @Override
    public void delete(UUID userId, String refreshToken) throws AuthException {
        Login log = loginJpaRepository
                .findByUserIdAndRefreshToken(userId, refreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        loginJpaRepository.delete(log);
    }

    @Override
    public Login find(UUID userId, String refreshToken) throws AuthException {
        return loginJpaRepository
                .findByUserIdAndRefreshToken(userId, refreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Override
    public void update(
            UUID userId, String oldRefreshToken, String newRefreshToken, LocalDateTime newRefreshTokenExpiredAt)
            throws AuthException {
        Login login = loginJpaRepository
                .findByUserIdAndRefreshToken(userId, oldRefreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        login.updateRefreshToken(newRefreshToken, newRefreshTokenExpiredAt);
        loginJpaRepository.save(login);
    }

    @Override
    public void create(Login login) {
        loginJpaRepository.save(login);
    }
}
