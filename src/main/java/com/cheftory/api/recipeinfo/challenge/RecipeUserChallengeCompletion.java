package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class RecipeUserChallengeCompletion extends MarketScope {

  @Id private UUID id;

  @Column(nullable = false)
  private UUID recipeUserChallengeId;

  @Column(nullable = false)
  private UUID recipeChallengeId;

  @Column(nullable = false)
  private LocalDateTime createdAt;
}
