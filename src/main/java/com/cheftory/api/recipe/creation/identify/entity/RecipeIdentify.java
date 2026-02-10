package com.cheftory.api.recipe.creation.identify.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 식별 엔티티
 *
 * <p>레시피 생성을 위해 사용자가 요청한 원본 URL을 저장하고, 중복 생성을 방지합니다.</p>
 * <p>마켓과 URL 조합으로 유니크 제약조건이 설정되어 있습니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_recipe_identify_market_video",
                    columnNames = {"market", "url"})
        })
public class RecipeIdentify extends MarketScope {
    /**
     * 식별자 ID
     */
    @Id
    private UUID id;

    /**
     * 원본 비디오 URL
     */
    @Column(nullable = false)
    private URI url;

    /**
     * 생성 일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 식별 엔티티 생성
     *
     * @param url 원본 비디오 URL
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 식별 엔티티
     */
    public static RecipeIdentify create(URI url, Clock clock) {
        return new RecipeIdentify(UUID.randomUUID(), url, clock.now());
    }
}
