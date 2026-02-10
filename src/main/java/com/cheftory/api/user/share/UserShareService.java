package com.cheftory.api.user.share;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareException;
import com.cheftory.api.user.share.port.UserShareCreditPort;
import com.cheftory.api.user.share.repository.UserShareRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 유저 공유 서비스
 *
 * <p>유저의 공유 횟수를 관리하고 크레딧을 지급합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserShareService {

    private final UserShareRepository repository;
    private final UserShareCreditPort creditPort;
    private final Clock clock;
    private static final int DAILY_SHARE_LIMIT = 3;

    /**
     * 유저 공유 처리
     *
     * <p>오늘의 공유 기록을 생성하고 공유 횟수를 증가시킨 후 크레딧을 지급합니다. 크레딧 지급 실패 시 트랜잭션 보상을 수행합니다.</p>
     *
     * @param userId 유저 ID
     * @return 공유 후의 총 공유 횟수
     * @throws UserShareException 공유 생성 또는 트랜잭션 처리 실패 시
     * @throws CreditException 크레딧 지급 실패 시
     */
    public int share(UUID userId) throws UserShareException, CreditException {
        UserShare userShare = repository.create(userId, clock);
        UserShare increased = repository.shareTx(userShare.getId(), DAILY_SHARE_LIMIT);

        try {
            creditPort.grantUserShare(userId, increased.getCount());
        } catch (CreditException e) {
            log.error("공유 크레딧 지급 실패. 보상 트랜잭션 실행: userId={}, count={}", userId, increased.getCount(), e);
            try {
                repository.compensateTx(userShare.getId(), userShare.getSharedAt());
            } catch (UserShareException ce) {
                log.error("공유 보상 실패(수동/배치 재처리 필요): userId={}, sharedAt={}", userId, userShare.getSharedAt(), ce);
            }
            throw e;
        }

        return increased.getCount();
    }
}
