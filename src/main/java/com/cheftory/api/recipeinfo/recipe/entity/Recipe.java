package com.cheftory.api.recipeinfo.recipe.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Recipe {
  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProcessStep processStep;

  private Integer viewCount;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  private RecipeStatus recipeStatus;

  public static Recipe create() {
    return Recipe.builder()
        .id(UUID.randomUUID())
        .processStep(ProcessStep.READY)
        .recipeStatus(RecipeStatus.IN_PROGRESS)
        .viewCount(0)
        .createdAt(LocalDateTime.now())
        .build();
  }

  public void success() {
    this.recipeStatus = RecipeStatus.SUCCESS;
  }

  public void failed() {
    this.recipeStatus = RecipeStatus.FAILED;
  }

  public boolean isSuccess() {
    return this.recipeStatus == RecipeStatus.SUCCESS;
  }

  public boolean isFailed() {
    return this.recipeStatus == RecipeStatus.FAILED;
  }
}
