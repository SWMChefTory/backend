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

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@Table(name = "recipe_category")
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

    public static RecipeCategory create(Clock clock, String name, UUID userId) {
        if (name == null || name.trim().isEmpty()) {
            throw new RecipeCategoryException(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
        }

        return new RecipeCategory(UUID.randomUUID(), name, userId, RecipeCategoryStatus.ACTIVE, clock.now());
    }

    public void delete() {
        this.status = RecipeCategoryStatus.DELETED;
    }
}
