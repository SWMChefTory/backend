package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@PocOnly(until = "2025-12-31")
public interface RecipeChallengeRepository extends JpaRepository<RecipeChallenge, UUID> {

    Page<RecipeChallenge> findAllByChallengeId(UUID challengeId, Pageable pageable);

    @Query(
            """
      select c
      from RecipeChallenge c
      where c.challengeId = :challengeId
      order by c.createdAt asc, c.id asc
      """)
    List<RecipeChallenge> findChallengeFirst(UUID challengeId, Pageable pageable);

    @Query(
            """
      select c
      from RecipeChallenge c
      where c.challengeId = :challengeId
        and (
          c.createdAt > :lastCreatedAt
          or (c.createdAt = :lastCreatedAt and c.id > :lastId)
        )
      order by c.createdAt asc, c.id asc
      """)
    List<RecipeChallenge> findChallengeKeyset(
            UUID challengeId, LocalDateTime lastCreatedAt, UUID lastId, Pageable pageable);
}
