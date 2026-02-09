package com.cheftory.api.user.share;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.share.dto.UserShareResponse;
import com.cheftory.api.user.share.exception.UserShareException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유저 공유 컨트롤러
 *
 * <p>유저 공유 API 요청을 처리합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/share")
public class UserShareController {

    private final UserShareService userShareService;

    /**
     * 유저 공유 요청 처리
     *
     * @param userId 인증된 유저 ID (@UserPrincipal로 주입됨)
     * @return 공유 횟수 응답 DTO
     */
    @PostMapping
    public UserShareResponse share(@UserPrincipal UUID userId) throws CreditException, UserShareException {
        int count = userShareService.share(userId);
        return UserShareResponse.of(count);
    }
}
