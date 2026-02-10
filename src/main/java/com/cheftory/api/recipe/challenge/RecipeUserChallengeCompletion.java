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
 * 사용자 챌린지 완료 엔티티.
 *
 * <p>사용자가 특정 챌린지를 완료한 이력을 관리합니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@PocOnly(until = "2025-12-31")
public class RecipeUserChallengeCompletion extends MarketScope {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipeUserChallengeId;

    @Column(nullable = false)
    private UUID recipeChallengeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
