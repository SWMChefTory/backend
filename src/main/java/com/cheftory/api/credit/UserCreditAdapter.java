package com.cheftory.api.credit;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.UserCreditPort;
import com.cheftory.api.user.exception.UserCreditException;
import com.cheftory.api.user.share.exception.UserShareCreditException;
import com.cheftory.api.user.share.port.UserShareCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자 관련 크레딧 어댑터.
 * User 모듈과 UserShare 모듈의 크레딧 포트를 구현합니다.
 */
@Component
@RequiredArgsConstructor
public class UserCreditAdapter implements UserShareCreditPort, UserCreditPort {
    private final CreditService creditService;
    private final Clock clock;

    /**
     * 공유 보상으로 크레딧을 지급합니다.
     *
     * @param userId 사용자 ID
     * @param count 공유 횟수
     * @throws UserShareCreditException 크레딧 관련 예외 발생 시
     */
    @Override
    public void grantUserShare(UUID userId, int count) throws UserShareCreditException {
        try {
            creditService.grant(Credit.share(userId, count, clock));
        } catch (CreditException exception) {
            throw new UserShareCreditException(exception.getError());
        }
    }

    /**
     * 튜토리얼 완료 보상으로 크레딧을 지급합니다.
     *
     * @param userId 사용자 ID
     * @throws UserCreditException 크레딧 관련 예외 발생 시
     */
    @Override
    public void grantUserTutorial(UUID userId) throws UserCreditException {
        try {
            creditService.grant(Credit.tutorial(userId));
        } catch (CreditException exception) {
            throw new UserCreditException(exception.getError());
        }
    }
}
