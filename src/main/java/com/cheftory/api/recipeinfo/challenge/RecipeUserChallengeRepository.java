package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

@PocOnly(until = "2025-12-31")
public interface RecipeUserChallengeRepository extends JpaRepository<RecipeUserChallenge, UUID> {
  List<RecipeUserChallenge> findRecipeUserChallengesByUserIdAndChallengeIdIn(
      UUID userId, List<UUID> challengeIds);

  RecipeUserChallenge findRecipeUserChallengeByUserIdAndChallengeId(UUID userId, UUID challengeId);
}
