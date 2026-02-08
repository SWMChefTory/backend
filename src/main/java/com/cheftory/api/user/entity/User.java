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

/**
 * 유저 엔티티
 *
 * <p>앱 사용자 정보를 저장하는 엔티티입니다. 소셜 로그인 제공자별 사용자 식별자를 통해 인증을 관리합니다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "`user`")
public class User {

    /**
     * 유저 고유 ID
     */
    @Id
    private UUID id;

    /**
     * 유저 닉네임
     */
    @Column(nullable = false, length = 20)
    private String nickname;

    /**
     * 성별
     */
    @Enumerated(EnumType.STRING)
    private Gender gender;

    /**
     * 생년월일
     */
    @Column
    private LocalDate dateOfBirth;

    /**
     * 유저 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    /**
     * 계정 생성일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 정보 수정일시
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 이용약관 동의일시
     */
    @Column(nullable = false)
    private LocalDateTime termsOfUseAgreedAt;

    /**
     * 개인정보 처리방침 동의일시
     */
    @Column(nullable = false)
    private LocalDateTime privacyAgreedAt;

    /**
     * 마케팅 정보 수신 동의일시
     */
    @Column(name = "marketing_agreed_at")
    private LocalDateTime marketingAgreedAt;

    /**
     * 튜토리얼 완료일시
     */
    @Column
    private LocalDateTime tutorialAt;

    /**
     * 소셜 로그인 제공자
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    /**
     * 소셜 로그인 제공자별 유저 고유 식별자
     */
    @Column(nullable = false, length = 127)
    private String providerSub;

    /**
     * 신규 유저 생성
     *
     * @param nickname 유저 닉네임
     * @param gender 성별
     * @param dateOfBirth 생년월일
     * @param provider 소셜 로그인 제공자
     * @param providerSub 제공자별 유저 고유 식별자
     * @param isMarketingAgreed 마케팅 정보 수신 동의 여부
     * @param clock 현재 시간 제공 객체
     * @return 생성된 유저 객체
     */
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

    /**
     * 유저 정보 변경
     *
     * @param nickname 수정할 닉네임
     * @param gender 수정할 성별
     * @param dateOfBirth 수정할 생년월일
     * @param clock 현재 시간 제공 객체
     */
    public void change(String nickname, Gender gender, LocalDate dateOfBirth, Clock clock) {
        this.nickname = nickname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.updatedAt = clock.now();
    }

    /**
     * 유저 상태 변경
     *
     * @param userStatus 변경할 유저 상태
     * @param clock 현재 시간 제공 객체
     */
    public void changeStatus(UserStatus userStatus, Clock clock) {
        this.userStatus = userStatus;
        this.updatedAt = clock.now();
    }

    /**
     * 튜토리얼 완료 처리
     *
     * @param clock 현재 시간 제공 객체
     */
    public void changeTutorial(Clock clock) {
        this.tutorialAt = clock.now();
    }
}
