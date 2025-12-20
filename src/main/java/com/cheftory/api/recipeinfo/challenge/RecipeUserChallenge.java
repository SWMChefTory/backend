package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@PocOnly(until = "2025-12-31")
public class RecipeUserChallenge extends MarketScope {

  @Id UUID id;

  @Column(nullable = false)
  UUID challengeId;

  @Column(nullable = false)
  UUID userId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RecipeUserChallengeStatus status;
}
