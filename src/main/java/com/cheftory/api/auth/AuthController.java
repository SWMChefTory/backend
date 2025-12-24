package com.cheftory.api.auth;

import com.cheftory.api.auth.dto.TokenReissueRequest;
import com.cheftory.api.auth.dto.TokenReissueResponse;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.auth.model.UserId;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/papi/v1/auth/extract-user-id")
  public ResponseEntity<UserId> loginWithOAuth(@RequestHeader("Authorization") String accessToken) {
    UUID userId =
        authService.extractUserIdFromToken(BearerAuthorizationUtils.removePrefix(accessToken));
    return ResponseEntity.ok(new UserId(userId.toString()));
  }

  @PostMapping("/api/v1/auth/token/reissue")
  public TokenReissueResponse reissueToken(@RequestBody TokenReissueRequest request) {
    AuthTokens authTokens =
        authService.reissue(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return TokenReissueResponse.from(authTokens);
  }
}
