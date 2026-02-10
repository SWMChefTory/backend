package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 챌린지 완료 기록 JPA 리포지토리.
 */
@PocOnly(until = "2025-12-31")
public interface RecipeUserChallengeCompletionRepository extends JpaRepository<RecipeUserChallengeCompletion, UUID> {
    /**
     * 레시피 챌린지 ID 목록과 사용자 챌린지 ID로 완료 기록을 조회합니다.
     *
     * @param recipeChallengeIds 레시피 챌린지 ID 목록
     * @param recipeUserChallengeId 사용자 챌린지 ID
     * @return 사용자 챌린지 완료 기록 목록
     */
    List<RecipeUserChallengeCompletion> findByRecipeChallengeIdInAndRecipeUserChallengeId(
            List<UUID> recipeChallengeIds, UUID recipeUserChallengeId);
}
