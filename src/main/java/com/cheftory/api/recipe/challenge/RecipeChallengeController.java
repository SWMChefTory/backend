package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.security.UserPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/challenge")
@PocOnly(until = "2025-12-31")
public class RecipeChallengeController {
  private final RecipeChallengeService recipeChallengeService;

  @GetMapping
  public ChallengeResponse getChallenge(@UserPrincipal UUID userId) {
    Challenge challenge = recipeChallengeService.getUser(userId);
    return ChallengeResponse.of(challenge);
  }
}
