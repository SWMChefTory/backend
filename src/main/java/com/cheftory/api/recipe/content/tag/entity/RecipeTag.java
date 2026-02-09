package com.cheftory.api.recipe.content.tag.entity;

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
 * 레시피 태그 엔티티
 *
 * <p>레시피를 분류하거나 검색하기 위한 키워드 정보를 저장하는 엔티티입니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeTag extends MarketScope {
    @Id
    private UUID id;

    /**
     * 태그 내용
     */
    @Column(nullable = false)
    private String tag;

    /**
     * 생성 일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 연결된 레시피 ID
     */
    @Column(nullable = false)
    private UUID recipeId;

    /**
     * 레시피 태그 생성
     *
     * @param tag 태그 내용
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 태그 엔티티
     */
    public static RecipeTag create(String tag, UUID recipeId, Clock clock) {
        return new RecipeTag(UUID.randomUUID(), tag, clock.now(), recipeId);
    }
}
