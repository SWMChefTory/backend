package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 레시피 챌린지 JPA 리포지토리.
 */
@PocOnly(until = "2025-12-31")
public interface RecipeChallengeRepository extends JpaRepository<RecipeChallenge, UUID> {
    /**
     * 챌린지의 첫 번째 레시피 챌린지 목록을 조회합니다.
     *
     * @param challengeId 챌린지 ID
     * @param pageable 페이지 정보
     * @return 레시피 챌린지 목록
     */
    @Query(
            """
            select c
            from RecipeChallenge c
      where c.challengeId = :challengeId
      order by c.createdAt asc, c.id asc
      """)
    List<RecipeChallenge> findChallengeFirst(UUID challengeId, Pageable pageable);

    /**
     * 챌린지의 다음 레시피 챌린지 목록을 키셋 기반으로 조회합니다.
     *
     * @param challengeId 챌린지 ID
     * @param lastCreatedAt 마지막 생성일시
     * @param lastId 마지막 ID
     * @param pageable 페이지 정보
     * @return 레시피 챌린지 목록
     */
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
