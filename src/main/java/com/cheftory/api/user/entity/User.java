package com.cheftory.api.user.entity;

import com.cheftory.api._common.Clock;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "`user`")
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

    @Column
    private LocalDateTime tutorialAt;

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
            boolean isMarketingAgreed,
            Clock clock) {
        return new User(
                UUID.randomUUID(),
                nickname,
                gender,
                dateOfBirth,
                UserStatus.ACTIVE,
                clock.now(),
                clock.now(),
                clock.now(),
                clock.now(),
                isMarketingAgreed ? clock.now() : null,
                null,
                provider,
                providerSub);
    }

    public void change(String nickname, Gender gender, LocalDate dateOfBirth, Clock clock) {
        this.nickname = nickname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.updatedAt = clock.now();
    }

    public void changeStatus(UserStatus userStatus, Clock clock) {
        this.userStatus = userStatus;
        this.updatedAt = clock.now();
    }

    public void changeTutorial(Clock clock) {
        this.tutorialAt = clock.now();
    }
}
