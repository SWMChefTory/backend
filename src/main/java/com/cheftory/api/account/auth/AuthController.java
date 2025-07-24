package com.cheftory.api.account.auth;

import com.cheftory.api.account.auth.dto.ExtractUserIdResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/papi/v1/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/extract-user-id")
  public ResponseEntity<ExtractUserIdResponse> loginWithOAuth(
      @RequestHeader("Authorization") String accessToken
  ) {
    UUID userId = authService.extractUserIdFromToken(accessToken);
    return ResponseEntity.ok(ExtractUserIdResponse.of(userId));
  }

}