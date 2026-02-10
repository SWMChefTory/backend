package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 챌린지 참여 JPA 리포지토리.
 */
@PocOnly(until = "2025-12-31")
public interface RecipeUserChallengeRepository extends JpaRepository<RecipeUserChallenge, UUID> {
    /**
     * 사용자의 챌린지 참여 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param challengeIds 챌린지 ID 목록
     * @return 사용자 챌린지 참여 목록
     */
    List<RecipeUserChallenge> findRecipeUserChallengesByUserIdAndChallengeIdIn(UUID userId, List<UUID> challengeIds);

    /**
     * 사용자의 특정 챌린지 참여 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param challengeId 챌린지 ID
     * @return 사용자 챌린지 참여 정보
     */
    RecipeUserChallenge findRecipeUserChallengeByUserIdAndChallengeId(UUID userId, UUID challengeId);
}
