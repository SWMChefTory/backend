package com.cheftory.api.recipe.content.scene.entity;

import com.cheftory.api._common.Clock;
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
 * 레시피 scene 엔티티.
 *
 * <p>step 단위로 추출된 조리 장면 정보를 저장합니다.</p>
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeScene extends MarketScope {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID stepId;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false, length = 30)
    private String label;

    @Column(name = "start_time", nullable = false)
    private Double start;

    @Column(name = "end_time", nullable = false)
    private Double end;

    @Column(nullable = false)
    private Integer importantScore;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static RecipeScene create(
            UUID stepId, UUID recipeId, String label, Double start, Double end, Integer importantScore, Clock clock) {
        return new RecipeScene(UUID.randomUUID(), stepId, recipeId, label, start, end, importantScore, clock.now());
    }
}
