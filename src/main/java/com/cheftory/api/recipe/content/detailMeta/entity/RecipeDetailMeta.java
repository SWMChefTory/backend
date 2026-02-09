package com.cheftory.api.recipe.content.detailMeta.entity;

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
 * 레시피 상세 메타 정보 엔티티
 *
 * <p>레시피의 조리 시간, 인분, 설명 등 추가적인 상세 정보를 저장하는 엔티티입니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeDetailMeta extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private Integer cookTime;

    @Column(nullable = false)
    private Integer servings;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private UUID recipeId;

    /**
     * 레시피 상세 메타 정보 생성
     *
     * @param cookTime 조리 시간 (분)
     * @param servings 인분
     * @param description 레시피 설명
     * @param clock 현재 시간 제공 객체
     * @param recipeId 연결된 레시피 ID
     * @return 생성된 레시피 상세 메타 정보 엔티티
     */
    public static RecipeDetailMeta create(
            Integer cookTime, Integer servings, String description, Clock clock, UUID recipeId) {

        return new RecipeDetailMeta(UUID.randomUUID(), cookTime, servings, description, clock.now(), recipeId);
    }
}
