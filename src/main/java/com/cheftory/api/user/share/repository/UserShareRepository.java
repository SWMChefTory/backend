package com.cheftory.api.user.share.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 유저 공유 Repository 인터페이스
 *
 * <p>유저 공유 기록을 관리하기 위한 도메인 Repository 인터페이스입니다.</p>
 */
public interface UserShareRepository {
    /**
     * 공유 횟수 증가 트랜잭션
     *
     * @param userShareId 공유 기록 ID
     * @param limit 일일 최대 공유 횟수
     * @return 횟수가 증가된 공유 기록
     * @throws UserShareException 공유 기록이 존재하지 않거나 제한 횟수 초과 시
     */
    UserShare shareTx(UUID userShareId, int limit) throws UserShareException;

    /**
     * 공유 보상 트랜잭션
     *
     * <p>크레딧 지급 실패 시 공유 횟수를 감소시키는 보상 작업을 수행합니다.</p>
     *
     * @param userShareId 공유 기록 ID
     * @param sharedAt 공유 일자
     * @throws UserShareException 공유 기록이 존재하지 않을 때
     */
    void compensateTx(UUID userShareId, LocalDate sharedAt) throws UserShareException;

    /**
     * 공유 기록 생성
     *
     * <p>오늘 날짜의 유저 공유 기록을 생성합니다. 이미 존재하면 기존 기록을 반환합니다.</p>
     *
     * @param userId 유저 ID
     * @param clock 현재 시간 제공 객체
     * @return 생성된 또는 기존의 공유 기록
     * @throws UserShareException 공유 기록 생성 실패 시
     */
    UserShare create(UUID userId, Clock clock) throws UserShareException;
}
