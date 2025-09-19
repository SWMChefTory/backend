package com.cheftory.api.recipeinfo.tag;

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
public class RecipeTag {
  @Id private UUID id;

  @Column(nullable = false)
  private String tag;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private UUID recipeId;

  public static RecipeTag create(String tag, UUID recipeId, Clock clock) {
    return RecipeTag.builder()
        .tag(tag)
        .createdAt(clock.now())
        .recipeId(recipeId)
        .id(UUID.randomUUID())
        .build();
  }
}
