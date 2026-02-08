package com.cheftory.api.user.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 유저 Repository 인터페이스
 */
public interface UserRepository {
    /**
     * 유저 ID로 유저 조회
     *
     * @param userId 유저 ID
     * @return 조회된 유저
     */
    User find(UUID userId) throws UserException;

    /**
     * Provider와 ProviderSub로 유저 조회
     *
     * @param provider 소셜 로그인 제공자
     * @param providerSub 제공자별 유저 고유 식별자
     * @return 조회된 유저
     */
    User find(Provider provider, String providerSub) throws UserException;

    /**
     * Provider와 ProviderSub로 유저 존재 여부 확인
     *
     * @param provider 소셜 로그인 제공자
     * @param providerSub 제공자별 유저 고유 식별자
     * @return 유저 존재 여부
     */
    boolean exist(Provider provider, String providerSub) throws UserException;

    /**
     * 유저 생성
     *
     * @param user 생성할 유저 엔티티
     * @return 저장된 유저
     */
    User create(User user);

    /**
     * 유저 정보 수정
     *
     * @param id 유저 ID
     * @param nickname 수정할 닉네임
     * @param gender 수정할 성별
     * @param dateOfBirth 수정할 생년월일
     * @param clock 현재 시간 제공 객체
     * @return 수정된 유저
     */
    User update(UUID id, String nickname, Gender gender, LocalDate dateOfBirth, Clock clock) throws UserException;

    /**
     * 유저 삭제 (DELETED 상태로 변경)
     *
     * @param id 유저 ID
     * @param clock 현재 시간 제공 객체
     */
    void delete(UUID id, Clock clock) throws UserException;

    /**
     * 유저 ID로 유저 존재 여부 확인
     *
     * @param userId 유저 ID
     * @return 유저 존재 여부
     */
    boolean exist(UUID userId);

    /**
     * 튜토리얼 완료 처리
     *
     * @param userId 유저 ID
     * @param clock 현재 시간 제공 객체
     */
    void completeTutorial(UUID userId, Clock clock) throws UserException;

    /**
     * 튜토리얼 완료 취소 처리
     *
     * @param userId 유저 ID
     * @param clock 현재 시간 제공 객체
     */
    void decompleteTutorial(UUID userId, Clock clock);
}
