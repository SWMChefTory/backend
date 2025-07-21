package com.cheftory.api.security;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sample")
public class SampleController {

  @GetMapping("/me")
  public ResponseEntity<String> me(@UserPrincipal UUID userId) {
    return ResponseEntity.ok("USER:" + userId);
  }

  @GetMapping("/public")
  public ResponseEntity<String> publicApi() {
    return ResponseEntity.ok("PUBLIC");
  }
}