package com.cheftory.api.account.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "login")
public class Login {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "login_ip")
  private String loginIp;

  @Column(name = "refresh_token", nullable = false, length = 255)
  private String refreshToken;

  @Column(name = "refresh_token_expired_at", nullable = false)
  private LocalDateTime refreshTokenExpiredAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "device_type", length = 20)
  private String deviceType;

  @Column(name = "country", length = 100)
  private String country;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  public static Login create(UUID userId, String refreshToken,
      LocalDateTime refreshTokenExpiredAt) {
    return Login.builder()
        .refreshToken(refreshToken)
        .refreshTokenExpiredAt(refreshTokenExpiredAt)
        .createdAt(LocalDateTime.now())
        .userId(userId)
        .build();
  }

  public void updateRefreshToken(String newToken, LocalDateTime expiredAt) {
    this.refreshToken = newToken;
    this.refreshTokenExpiredAt = expiredAt;
  }
}