package com.cheftory.api.recipe.content.ingredient.entity;

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
 * 레시피 재료 엔티티
 *
 * <p>레시피를 구성하는 개별 재료의 이름, 단위, 양 정보를 저장하는 엔티티입니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeIngredient extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 재료 생성
     *
     * @param name 재료 이름
     * @param unit 단위 (예: g, ml, 개)
     * @param amount 양
     * @param recipeId 연결된 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 재료 엔티티
     */
    public static RecipeIngredient create(String name, String unit, Integer amount, UUID recipeId, Clock clock) {

        return new RecipeIngredient(UUID.randomUUID(), name, unit, amount, recipeId, clock.now());
    }
}
