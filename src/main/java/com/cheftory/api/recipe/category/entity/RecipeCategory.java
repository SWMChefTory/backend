package com.cheftory.api.recipe.category.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 카테고리 엔티티
 *
 * <p>사용자가 정의한 레시피 분류 정보를 저장하는 엔티티입니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@Table
public class RecipeCategory extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private RecipeCategoryStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 레시피 카테고리 생성
     *
     * @param clock 현재 시간 제공 객체
     * @param name 카테고리 이름
     * @param userId 유저 ID
     * @return 생성된 레시피 카테고리 엔티티
     * @throws RecipeCategoryException 이름이 비어있을 때 RECIPE_CATEGORY_NAME_EMPTY
     */
    public static RecipeCategory create(Clock clock, String name, UUID userId) throws RecipeCategoryException {
        if (name == null || name.trim().isEmpty()) {
            throw new RecipeCategoryException(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
        }

        return new RecipeCategory(UUID.randomUUID(), name, userId, RecipeCategoryStatus.ACTIVE, clock.now());
    }

    /**
     * 레시피 카테고리 삭제 처리 (상태 변경)
     */
    public void delete() {
        this.status = RecipeCategoryStatus.DELETED;
    }
}
