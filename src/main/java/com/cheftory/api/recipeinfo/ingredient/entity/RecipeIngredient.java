package com.cheftory.api.recipeinfo.ingredient;

import com.cheftory.api._common.Clock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class RecipeIngredient {
  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String unit;

  @Column(nullable = false)
  private Integer amount;

  @Column(nullable = false)
  private UUID recipeId;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public static RecipeIngredient create(
      String name, String unit, Integer amount, UUID recipeId, Clock clock) {
    return RecipeIngredient.builder()
        .id(UUID.randomUUID())
        .name(name)
        .unit(unit)
        .amount(amount)
        .recipeId(recipeId)
        .createdAt(clock.now())
        .build();
  }
}
