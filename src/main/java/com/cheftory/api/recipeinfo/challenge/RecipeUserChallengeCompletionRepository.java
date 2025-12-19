package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

@PocOnly(until = "2025-12-31")
public interface RecipeUserChallengeCompletionRepository
    extends JpaRepository<RecipeUserChallengeCompletion, UUID> {
  List<RecipeUserChallengeCompletion> findByRecipeChallengeIdInAndRecipeUserChallengeId(
      List<UUID> recipeChallengeIds, UUID recipeUserChallengeId);
}
