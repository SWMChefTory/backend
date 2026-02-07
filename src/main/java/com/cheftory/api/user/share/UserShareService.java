package com.cheftory.api.user.share;

import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserShareService {

    private final UserShareTxService userShareTxService;
    private final UserShareCreditPort userShareCreditPort;

    public int share(UUID userId) {
        UserShare userShare = userShareTxService.shareTx(userId);

        try {
            userShareCreditPort.grantUserShare(userId, userShare.getCount());
        } catch (CreditException e) {
            log.error("공유 크레딧 지급 실패. 보상 트랜잭션 실행: userId={}, count={}", userId, userShare.getCount(), e);
            try {
                userShareTxService.compensateTx(userId, userShare.getSharedAt());
            } catch (CreditException ce) {
                log.error("공유 보상 실패(수동/배치 재처리 필요): userId={}, sharedAt={}", userId, userShare.getSharedAt(), ce);
            }
            throw e;
        }

        return userShare.getCount();
    }
}
