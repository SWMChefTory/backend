package com.cheftory.api.user.share;

import java.util.UUID;

public interface UserShareCreditPort {
	void grantUserShare(UUID userId, int count);
}
