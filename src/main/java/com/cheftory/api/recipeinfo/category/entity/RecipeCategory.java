package com.cheftory.api.recipeinfo.category;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipeinfo.category.exception.RecipeCategoryException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeCategory {
  @Id private UUID id;

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
    return RecipeCategory.builder()
        .id(UUID.randomUUID())
        .name(name)
        .userId(userId)
        .createdAt(clock.now())
        .status(RecipeCategoryStatus.ACTIVE)
        .build();
  }

  public void delete() {
    this.status = RecipeCategoryStatus.DELETED;
  }
}
