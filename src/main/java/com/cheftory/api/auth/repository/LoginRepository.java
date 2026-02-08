package com.cheftory.api.auth.repository;

import com.cheftory.api.auth.entity.Login;
import com.cheftory.api.auth.exception.AuthException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 로그인 리포지토리 인터페이스
 */
public interface LoginRepository {
    /**
     * 로그인 삭제 (로그아웃)
     *
     * @param userId 유저 ID
     * @param refreshToken 리프레시 토큰
     * @throws AuthException 로그인을 찾을 수 없을 때 INVALID_REFRESH_TOKEN
     */
    void delete(UUID userId, String refreshToken) throws AuthException;

    /**
     * 로그인 조회
     *
     * @param userId 유저 ID
     * @param refreshToken 리프레시 토큰
     * @return 조회된 로그인 엔티티
     * @throws AuthException 로그인을 찾을 수 없을 때 INVALID_REFRESH_TOKEN
     */
    Login find(UUID userId, String refreshToken) throws AuthException;

    /**
     * 로그인 리프레시 토큰 갱신
     *
     * @param userId 유저 ID
     * @param oldRefreshToken 기존 리프레시 토큰
     * @param newRefreshToken 새로운 리프레시 토큰
     * @param newRefreshTokenExpiredAt 새로운 리프레시 토큰 만료 시간
     * @throws AuthException 로그인을 찾을 수 없을 때 INVALID_REFRESH_TOKEN
     */
    void update(UUID userId, String oldRefreshToken, String newRefreshToken, LocalDateTime newRefreshTokenExpiredAt)
            throws AuthException;

    /**
     * 로그인 생성
     *
     * @param login 생성할 로그인 엔티티
     */
    void create(Login login);
}
