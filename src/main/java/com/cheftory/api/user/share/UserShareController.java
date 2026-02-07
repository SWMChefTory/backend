package com.cheftory.api.user.share;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/share")
public class UserShareController {

	private final UserShareService userShareService;

	@PostMapping
	public UserShareResponse share(@UserPrincipal UUID userId) {
		int count = userShareService.share(userId);
		return UserShareResponse.of(count);
	}
}
