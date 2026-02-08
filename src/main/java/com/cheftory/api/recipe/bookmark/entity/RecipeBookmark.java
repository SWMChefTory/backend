package com.cheftory.api.recipe.bookmark.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 북마크 엔티티
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_recipe_bookmark_user_recipe",
                    columnNames = {"user_id", "recipe_id"})
        })
public class RecipeBookmark extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime viewedAt;

    private Integer lastPlaySeconds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID recipeId;

    @Column(nullable = true)
    private UUID recipeCategoryId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecipeBookmarkStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    /**
     * 레시피 북마크 엔티티 생성
     *
     * @param clock 시계
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @return 생성된 북마크 엔티티
     */
    public static RecipeBookmark create(Clock clock, UUID userId, UUID recipeId) {
        return new RecipeBookmark(
                UUID.randomUUID(),
                clock.now(),
                0,
                clock.now(),
                clock.now(),
                userId,
                recipeId,
                null,
                RecipeBookmarkStatus.ACTIVE,
                0L);
    }

    /**
     * 레시피 카테고리 ID 설정
     *
     * @param recipeCategoryId 카테고리 ID
     */
    public void updateRecipeCategoryId(UUID recipeCategoryId) {
        this.recipeCategoryId = recipeCategoryId;
    }

    /**
     * 레시피 카테고리 ID 초기화
     */
    public void emptyRecipeCategoryId() {
        this.recipeCategoryId = null;
    }

    /**
     * 최근 조회 시간 업데이트
     *
     * @param clock 시계
     */
    public void updateViewedAt(Clock clock) {
        this.viewedAt = clock.now();
    }

    /**
     * 레시피 북마크 차단
     *
     * @param clock 시계
     */
    public void block(Clock clock) {
        this.status = RecipeBookmarkStatus.BLOCKED;
        this.updatedAt = clock.now();
    }

    /**
     * 레시피 북마크 삭제
     *
     * @param clock 시계
     */
    public void delete(Clock clock) {
        this.status = RecipeBookmarkStatus.DELETED;
        this.updatedAt = clock.now();
    }

    /**
     * 레시피 북마크 활성화
     *
     * @param clock 시계
     */
    public void active(Clock clock) {
        this.status = RecipeBookmarkStatus.ACTIVE;
        this.updatedAt = clock.now();
    }
}
