package com.cheftory.api.account.user.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // builder()로만 생성하려면 유지 가능
@Table(name = "user")
@Builder
public class User {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(nullable = false, length = 20)
  private String nickname;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "terms_agreed_at", nullable = false)
  private LocalDateTime termsAgreedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  @Column(nullable = false, length = 127)
  private String providerSub;

  public static User create(
      String email,
      String nickname,
      Gender gender,
      LocalDate dateOfBirth,
      Provider provider,
      String providerSub
  ) {
    return User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .nickname(nickname)
        .gender(gender)
        .dateOfBirth(dateOfBirth)
        .status(Status.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .termsAgreedAt(LocalDateTime.now())
        .provider(provider)
        .providerSub(providerSub)
        .build();
  }

  public void changeStatus(Status status) {
    this.status = status;
    this.updatedAt = LocalDateTime.now();
  }
}