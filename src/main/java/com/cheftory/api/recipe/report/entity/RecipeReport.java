package com.cheftory.api.recipe.report.entity;

import com.cheftory.api._common.Clock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 신고 엔티티
 */
@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_report_user_recipe",
                    columnNames = {"reporter_id", "recipe_id"})
        })
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class RecipeReport {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = false)
    private UUID reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipeReportReason reason;

    @Column(length = 500, nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 신고 엔티티 생성
     *
     * @param clock 시계
     * @param reporterId 신고자 ID
     * @param recipeId 레시피 ID
     * @param reason 신고 사유
     * @param description 상세 설명
     * @return 생성된 신고 엔티티
     */
    public static RecipeReport create(
            Clock clock, UUID reporterId, UUID recipeId, RecipeReportReason reason, String description) {
        return new RecipeReport(
                UUID.randomUUID(), recipeId, reporterId, reason, description == null ? "" : description, clock.now());
    }
}
