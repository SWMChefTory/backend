package com.cheftory.api.recipe.challenge;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 챌린지 엔티티.
 *
 * <p>특정 레시피가 속한 챌린지 정보를 관리합니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@PocOnly(until = "2025-12-31")
public class RecipeChallenge extends MarketScope {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private UUID challengeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
