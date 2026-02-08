package com.cheftory.api.auth;

import com.cheftory.api.auth.dto.TokenReissueRequest;
import com.cheftory.api.auth.dto.TokenReissueResponse;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.auth.dto.UserIdResponse;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API 엔드포인트를 제공하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

    private final AuthService authService;

    /**
     * OAuth 액세스 토큰에서 유저 ID 추출
     *
     * @param accessToken Bearer 스킴이 포함된 액세스 토큰
     * @return 추출된 유저 ID
     * @throws AuthException 토큰이 유효하지 않을 때 INVALID_ACCESS_TOKEN
     * @throws AuthException 토큰이 만료되었을 때 EXPIRED_TOKEN
     */
    @PostMapping("/papi/v1/auth/extract-user-id")
    public UserIdResponse loginWithOAuth(@RequestHeader("Authorization") String accessToken) {
        UUID userId = authService.extractUserIdFromToken(
                BearerAuthorizationUtils.removePrefix(accessToken), AuthTokenType.ACCESS);
        return UserIdResponse.of(userId);
    }

    /**
     * 리프레시 토큰으로 새로운 토큰 재발급
     *
     * @param request 리프레시 토큰이 포함된 요청
     * @return 새로 발급된 액세스 토큰과 리프레시 토큰
     * @throws AuthException 리프레시 토큰이 유효하지 않을 때 INVALID_REFRESH_TOKEN
     * @throws AuthException 리프레시 토큰이 만료되었을 때 EXPIRED_TOKEN
     */
    @PostMapping("/api/v1/auth/token/reissue")
    public TokenReissueResponse reissueToken(@RequestBody TokenReissueRequest request) {
        AuthTokens authTokens = authService.reissue(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
        return TokenReissueResponse.from(authTokens);
    }
}
