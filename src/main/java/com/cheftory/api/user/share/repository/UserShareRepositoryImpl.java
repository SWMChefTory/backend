package com.cheftory.api.user.share.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

/**
 * 유저 공유 저장소 구현체
 *
 * <p>유저 공유 기록을 관리하고 트랜잭션 처리를 수행합니다.</p>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserShareRepositoryImpl implements UserShareRepository {
    private final UserShareJpaRepository userShareJpaRepository;

    /**
     * 공유 횟수 증가 트랜잭션
     *
     * <p>UserShare ID로 공유 기록을 조회하고 횟수를 증가시킵니다. 낙관락 충돌 시 재시도합니다.</p>
     *
     * @param userShareId 공유 기록 ID
     * @param limit 일일 최대 공유 횟수
     * @return 횟수가 증가된 UserShare 엔티티
     * @throws UserShareException 공유 기록이 존재하지 않거나 제한 횟수 초과 시
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    @Override
    public UserShare shareTx(UUID userShareId, int limit) throws UserShareException {
        UserShare userShare = userShareJpaRepository
                .findById(userShareId)
                .orElseThrow(() -> new UserShareException(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED));

        userShare.increase(limit);

        userShareJpaRepository.save(userShare);
        return userShare;
    }

    /**
     * 공유 보상 트랜잭션
     *
     * <p>UserShare ID로 공유 기록을 조회하고 횟수를 감소시킵니다. 크레딧 지급 실패 시 보상용으로 사용됩니다. 낙관락 충돌 시 재시도합니다.</p>
     *
     * @param userShareId 공유 기록 ID
     * @param sharedAt 공유 일자
     * @throws UserShareException 공유 기록이 존재하지 않을 때
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    @Override
    public void compensateTx(UUID userShareId, LocalDate sharedAt) throws UserShareException {
        UserShare userShare = userShareJpaRepository
                .findById(userShareId)
                .orElseThrow(() -> new UserShareException(UserShareErrorCode.USER_SHARE_NOT_FOUND));

        userShare.decrease();

        userShareJpaRepository.save(userShare);
    }

    /**
     * 공유 기록 생성
     *
     * <p>오늘 날짜의 유저 공유 기록을 생성합니다. 동시 생성 요청이 들어오면 기존 데이터를 조회하여 반환합니다.</p>
     *
     * @param userId 유저 ID
     * @param clock 시계
     * @return 생성된 UserShare 엔티티
     * @throws UserShareException 공유 기록 생성 실패 시
     */
    @Override
    public UserShare create(UUID userId, Clock clock) throws UserShareException {
        LocalDate today = clock.now().toLocalDate();
        try {
            return userShareJpaRepository.save(UserShare.create(userId, today, clock));
        } catch (DataIntegrityViolationException e) {
            return userShareJpaRepository
                    .findByUserIdAndSharedAt(userId, today)
                    .orElseThrow(() -> new UserShareException(UserShareErrorCode.USER_SHARE_NOT_FOUND));
        }
    }

    /**
     * 공유 생성 실패 시 복구 메서드
     *
     * @param e 낙관락 예외
     * @param userShareId 유저 공유 ID
     * @return 공유 엔티티
     * @throws UserShareException 공유 생성 실패 시
     */
    @Recover
    public UserShare recover(ObjectOptimisticLockingFailureException e, UUID userShareId, int limit)
            throws UserShareException {
        throw new UserShareException(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
    }

    /**
     * 공유 보상 실패 시 복구 메서드
     *
     * <p>로그를 남기고 아무 작업도 수행하지 않습니다.</p>
     *
     * @param e 낙관락 예외
     * @param userShareId 유저 공유 ID
     * @param sharedAt 공유 일시
     */
    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, UUID userShareId, LocalDate sharedAt)
            throws UserShareException {
        throw new UserShareException(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
    }
}
