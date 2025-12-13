package com.cheftory.api.recipeinfo.detailMeta;

import com.cheftory.api._common.Clock;
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
public class RecipeDetailMeta {
  @Id private UUID id;

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

  public static RecipeDetailMeta create(
      Integer cookTime, Integer servings, String description, Clock clock, UUID recipeId) {
    return RecipeDetailMeta.builder()
        .id(UUID.randomUUID())
        .cookTime(cookTime)
        .servings(servings)
        .description(description)
        .createdAt(clock.now())
        .recipeId(recipeId)
        .build();
  }
}
