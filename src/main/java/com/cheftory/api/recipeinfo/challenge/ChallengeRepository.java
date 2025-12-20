package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.PocOnly;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@PocOnly(until = "2025-12-31")
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

  @Query(
      """
      select c
      from Challenge c
      where c.startAt <= :now
        and c.endAt >= :now
      """)
  List<Challenge> findOngoing(@Param("now") LocalDateTime now);
}
