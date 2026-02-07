package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.user.UserCreditPort;
import com.cheftory.api.user.share.UserShareCreditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class UserCreditAdapter implements UserShareCreditPort, UserCreditPort {
	private final CreditService creditService;

	@Override
	public void grantUserShare(UUID userId, int count) {
		creditService.grant(Credit.share(userId, count));
	}

	@Override
	public void grantUserTutorial(UUID userId) {
		creditService.grant(Credit.tutorial(userId));
	}
}
