package com.cheftory.api.security;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class SecurityController {

  @GetMapping("/api/security/failed")
  public ResponseEntity<String> failedPublicPrincipal(@UserPrincipal UUID userId) {
    return ResponseEntity.ok("success");
  }

  @GetMapping("/api/security/success")
  public ResponseEntity<String> successPublicPrincipal(@UserPrincipal UUID userId) {
    return ResponseEntity.ok(userId.toString());
  }

  @GetMapping("/papi/v1/security/success")
  public ResponseEntity<String> truePrivatePrincipal() {
    return ResponseEntity.ok("success");
  }

  @GetMapping("/api/v1/account/security/success")
  public ResponseEntity<String> successAccountSecurity() {
    return ResponseEntity.ok("success");
  }
}