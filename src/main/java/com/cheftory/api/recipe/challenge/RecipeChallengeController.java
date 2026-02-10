package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 챌린지 관련 API 요청을 처리하는 컨트롤러.
 *
 * <p>사용자의 챌린지 정보를 제공합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes/challenge")
@PocOnly(until = "2025-12-31")
public class RecipeChallengeController {
    private final RecipeChallengeService recipeChallengeService;

    /**
     * 사용자의 챌린지 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 챌린지 정보 응답
     * @throws RecipeChallengeException 챌린지 조회 실패 시
     */
    @GetMapping
    public ChallengeResponse getChallenge(@UserPrincipal UUID userId) throws RecipeChallengeException {
        Challenge challenge = recipeChallengeService.getUser(userId);
        return ChallengeResponse.of(challenge);
    }
}
