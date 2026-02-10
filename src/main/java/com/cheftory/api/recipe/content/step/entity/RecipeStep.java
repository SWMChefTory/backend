package com.cheftory.api.recipe.content.step.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 레시피 단계 엔티티
 *
 * <p>레시피의 조리 순서, 자막, 상세 내용 및 영상 시작 시간 정보를 저장하는 엔티티입니다.</p>
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeStep extends MarketScope {

    /**
     * 레시피 단계 상세 정보 (JSON)
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Detail {
        /**
         * 텍스트 내용
         */
        private String text;
        /**
         * 시작 시간 (초)
         */
        private Double start;

        /**
         * 상세 정보 생성
         *
         * @param text 텍스트
         * @param start 시작 시간
         * @return 상세 정보 객체
         */
        public static Detail of(String text, Double start) {
            return new Detail(text, start);
        }
    }

    @Id
    private UUID id;

    /**
     * 단계 순서
     */
    private Integer stepOrder;

    /**
     * 자막/소제목
     */
    private String subtitle;

    /**
     * 상세 내용 목록 (JSON 타입으로 저장)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Detail> details;

    /**
     * 단계 시작 시간 (초)
     */
    private Double start;

    /**
     * 연결된 레시피 ID
     */
    private UUID recipeId;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 레시피 단계 생성
     *
     * @param stepOrder 순서
     * @param subtitle 소제목
     * @param details 상세 내용 목록
     * @param start 시작 시간
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 생성된 레시피 단계 엔티티
     */
    public static RecipeStep create(
            Integer stepOrder, String subtitle, List<Detail> details, Double start, UUID recipeId, Clock clock) {

        return new RecipeStep(UUID.randomUUID(), stepOrder, subtitle, details, start, recipeId, clock.now());
    }
}
