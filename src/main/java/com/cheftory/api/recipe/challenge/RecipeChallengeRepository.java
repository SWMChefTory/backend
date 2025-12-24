package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@PocOnly(until = "2025-12-31")
public interface RecipeChallengeRepository extends JpaRepository<RecipeChallenge, UUID> {

  Page<RecipeChallenge> findAllByChallengeId(UUID challengeId, Pageable pageable);
}
