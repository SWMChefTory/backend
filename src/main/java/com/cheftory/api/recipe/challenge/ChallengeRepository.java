package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 챌린지 JPA 리포지토리.
 */
@PocOnly(until = "2025-12-31")
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    /**
     * 진행 중인 챌린지 목록을 조회합니다.
     *
     * @param now 현재 시간
     * @return 진행 중인 챌린지 목록
     */
    @Query("""
      select c
      from Challenge c
      where c.startAt <= :now
        and c.endAt >= :now
      """)
    List<Challenge> findOngoing(@Param("now") LocalDateTime now);
}
