package com.cheftory.api.auth.entity;

import com.cheftory.api._common.Clock;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

/**
 * 로그인 세션 정보를 담는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table
public class Login {

    @Id
    private UUID id;

    @Column(nullable = false, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime refreshTokenExpiredAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private UUID userId;

    /**
     * Login 엔티티 생성
     *
     * @param userId 유저 ID
     * @param refreshToken 리프레시 토큰
     * @param refreshTokenExpiredAt 리프레시 토큰 만료 시간
     * @param clock 현재 시간 제공자
     * @return Login 엔티티
     */
    public static Login create(UUID userId, String refreshToken, LocalDateTime refreshTokenExpiredAt, Clock clock) {
        return new Login(UUID.randomUUID(), refreshToken, refreshTokenExpiredAt, clock.now(), userId);
    }

    /**
     * 리프레시 토큰 갱신
     *
     * @param newToken 새로운 리프레시 토큰
     * @param expiredAt 리프레시 토큰 만료 시간
     */
    public void updateRefreshToken(String newToken, LocalDateTime expiredAt) {
        this.refreshToken = newToken;
        this.refreshTokenExpiredAt = expiredAt;
    }
}
