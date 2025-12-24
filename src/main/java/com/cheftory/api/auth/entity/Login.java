package com.cheftory.api.auth.entity;

import com.cheftory.api._common.Clock;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table
public class Login {

  @Id private UUID id;

  @Column(name = "refresh_token", nullable = false, length = 512)
  private String refreshToken;

  @Column(name = "refresh_token_expired_at", nullable = false)
  private LocalDateTime refreshTokenExpiredAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  public static Login create(
      UUID userId, String refreshToken, LocalDateTime refreshTokenExpiredAt, Clock clock) {
    return new Login(UUID.randomUUID(), refreshToken, refreshTokenExpiredAt, clock.now(), userId);
  }

  public void updateRefreshToken(String newToken, LocalDateTime expiredAt) {
    this.refreshToken = newToken;
    this.refreshTokenExpiredAt = expiredAt;
  }
}
