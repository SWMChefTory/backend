package com.cheftory.api.user.share;

import com.cheftory.api._common.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserShareTxService {

    private static final int DAILY_LIMIT = 3;

    private final UserShareRepository userShareRepository;
    private final Clock clock;

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            notRetryFor = UserShareException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    @Transactional
    public UserShare shareTx(UUID userId) {
        LocalDate today = clock.now().toLocalDate();

        UserShare userShare =
                userShareRepository.findByUserIdAndSharedAt(userId, today).orElseGet(() -> create(userId, today));

        userShare.increase(DAILY_LIMIT);

        userShareRepository.flush();
        return userShare;
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 30, multiplier = 2.0))
    @Transactional
    public void compensateTx(UUID userId, LocalDate sharedAt) {
        userShareRepository.findByUserIdAndSharedAt(userId, sharedAt).ifPresent(userShare -> {
            userShare.decrease();
            userShareRepository.flush();
        });
    }

    private UserShare create(UUID userId, LocalDate today) {
        try {
            return userShareRepository.save(UserShare.create(userId, today, clock));
        } catch (DataIntegrityViolationException e) {
            return userShareRepository.findByUserIdAndSharedAt(userId, today).orElseThrow(() -> e);
        }
    }

    @Recover
    public UserShare recover(ObjectOptimisticLockingFailureException e, UUID userId) {
        throw new UserShareException(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
    }

    @Recover
    public UserShare recover(UserShareException e, UUID userId) {
        // 비즈니스 로직 에러(3회 초과 등)는 그대로 다시 던지기
        throw e;
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, UUID userId, LocalDate sharedAt) {
        // 보상 트랜잭션 실패 시 로그를 남기고 에러를 다시 던짐
        throw new UserShareException(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
    }
}
