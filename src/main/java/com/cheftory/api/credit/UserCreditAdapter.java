package com.cheftory.api.credit;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.user.UserCreditPort;
import com.cheftory.api.user.share.port.UserShareCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreditAdapter implements UserShareCreditPort, UserCreditPort {
    private final CreditService creditService;
    private final Clock clock;

    @Override
    public void grantUserShare(UUID userId, int count) {
        creditService.grant(Credit.share(userId, count, clock));
    }

    @Override
    public void grantUserTutorial(UUID userId) {
        creditService.grant(Credit.tutorial(userId));
    }
}
