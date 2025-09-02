package com.cheftory.api.account.user.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "`user`")
@Builder(toBuilder = true)
public class User {

  @Id
  private UUID id;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus userStatus;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "terms_of_use_agreed_at", nullable = false)
  private LocalDateTime termsOfUseAgreedAt;

  @Column(name = "privacy_agreed_at", nullable = false)
  private LocalDateTime privacyAgreedAt;

  @Column(name = "marketing_agreed_at")
  private LocalDateTime marketingAgreedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  @Column(nullable = false, length = 127)
  private String providerSub;

  public static User create(
      String nickname,
      Gender gender,
      LocalDate dateOfBirth,
      Provider provider,
      String providerSub,
      boolean isMarketingAgreed
  ) {
    return User.builder()
        .id(UUID.randomUUID())
        .nickname(nickname)
        .gender(gender)
        .dateOfBirth(dateOfBirth)
        .userStatus(UserStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .termsOfUseAgreedAt(LocalDateTime.now())
        .privacyAgreedAt(LocalDateTime.now())
        .marketingAgreedAt(isMarketingAgreed ? LocalDateTime.now() : null)
        .provider(provider)
        .providerSub(providerSub)
        .build();
  }

  public void changeStatus(UserStatus userStatus) {
    this.userStatus = userStatus;
    this.updatedAt = LocalDateTime.now();
  }
}