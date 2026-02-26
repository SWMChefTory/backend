package com.cheftory.api.auth;

import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.entity.Login;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.jwt.TokenProvider;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.auth.repository.LoginRepository;
import com.cheftory.api.auth.verifier.AppleTokenVerifier;
import com.cheftory.api.auth.verifier.GoogleTokenVerifier;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.cheftory.api.user.entity.Provider;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증 도메인의 비즈니스 로직을 처리하는 서비스
 */
@RequiredArgsConstructor
@Service
public class AuthService {

    private final GoogleTokenVerifier googleVerifier;
    private final AppleTokenVerifier appleVerifier;
    private final TokenProvider jwtProvider;
    private final LoginRepository loginRepository;
    private final Clock clock;

    /**
     * ID 토큰에서 제공자별 유저 식별자 추출
     *
     * @param idToken OAuth 제공자로부터 받은 ID 토큰
     * @param provider 외부 인증 제공자 (GOOGLE, APPLE 등)
     * @return 제공자별 유저 고유 식별자
     * @throws AuthException 지원하지 않는 제공자일 때 UNSUPPORTED_PROVIDER
     * @throws AuthException ID 토큰이 유효하지 않을 때 INVALID_ID_TOKEN
     */
    public String extractProviderSubFromIdToken(String idToken, Provider provider) throws AuthException {
        try {
            return switch (provider) {
                case GOOGLE -> googleVerifier.getSubFromToken(idToken);
                case APPLE -> appleVerifier.getSubFromToken(idToken);
                default -> throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
            };
        } catch (VerificationException e) {
            throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN, e);
        }
    }

    /**
     * 토큰에서 유저 ID 추출
     *
     * @param token 검증할 JWT 토큰
     * @param tokenType 토큰 타입 (ACCESS, REFRESH)
     * @return 토큰에 포함된 유저 ID
     * @throws AuthException 토큰이 유효하지 않을 때 INVALID_ACCESS_TOKEN 또는 INVALID_REFRESH_TOKEN
     * @throws AuthException 토큰이 만료되었을 때 EXPIRED_TOKEN
     */
    public UUID extractUserIdFromToken(String token, AuthTokenType tokenType) throws AuthException {
        return jwtProvider.getUserId(token, tokenType);
    }

    /**
     * 리프레시 토큰으로 새로운 토큰 재발급
     *
     * @param refreshToken 기존 리프레시 토큰
     * @return 새로 발급된 액세스 토큰과 리프레시 토큰
     * @throws AuthException 리프레시 토큰이 유효하지 않을 때 INVALID_REFRESH_TOKEN
     * @throws AuthException 리프레시 토큰이 만료되었을 때 EXPIRED_TOKEN
     */
    public AuthTokens reissue(String refreshToken) throws AuthException {

        UUID userId = extractUserIdFromToken(refreshToken, AuthTokenType.REFRESH);

        AuthTokens authTokens = createAuthToken(userId);
        updateRefreshToken(userId, refreshToken, authTokens.refreshToken());
        return authTokens;
    }

    /**
     * 유저 ID로 액세스 토큰과 리프레시 토큰 생성
     *
     * @param userId 유저 ID
     * @return 생성된 액세스 토큰과 리프레시 토큰
     */
    public AuthTokens createAuthToken(UUID userId) {
        String accessToken = jwtProvider.createToken(userId, AuthTokenType.ACCESS);
        String refreshToken = jwtProvider.createToken(userId, AuthTokenType.REFRESH);
        return AuthTokens.of(accessToken, refreshToken);
    }

    /**
     * 로그인 세션 저장
     *
     * @param userId 유저 ID
     * @param refreshToken 저장할 리프레시 토큰
     * @throws AuthException 토큰 만료 시간 조회 실패 시
     */
    public void saveLoginSession(UUID userId, String refreshToken) throws AuthException {
        LocalDateTime refreshTokenExpiredAt = jwtProvider.getExpiration(refreshToken);
        Login login = Login.create(userId, refreshToken, refreshTokenExpiredAt, clock);
        loginRepository.create(login);
    }

    /**
     * 리프레시 토큰 갱신
     *
     * @param userId 유저 ID
     * @param oldRefreshToken 기존 리프레시 토큰
     * @param newRefreshToken 새로운 리프레시 토큰
     * @throws AuthException 토큰 갱신 실패 시
     */
    private void updateRefreshToken(UUID userId, String oldRefreshToken, String newRefreshToken) throws AuthException {
        LocalDateTime refreshTokenExpiredAt = jwtProvider.getExpiration(newRefreshToken);
        loginRepository.update(userId, oldRefreshToken, newRefreshToken, refreshTokenExpiredAt);
    }

    /**
     * 리프레시 토큰 삭제 (로그아웃)
     *
     * @param userId 유저 ID
     * @param refreshToken 삭제할 리프레시 토큰
     * @throws AuthException 토큰 삭제 실패 시
     */
    public void deleteRefreshToken(UUID userId, String refreshToken) throws AuthException {
        loginRepository.delete(userId, refreshToken);
    }
}
