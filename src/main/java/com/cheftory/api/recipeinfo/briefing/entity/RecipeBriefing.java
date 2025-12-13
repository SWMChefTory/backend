package com.cheftory.api.recipeinfo.briefing;

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
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeBriefing {
  @Id private UUID id;

  @Column(nullable = false)
  private UUID recipeId;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public static RecipeBriefing create(UUID recipeId, String content, Clock clock) {
    return RecipeBriefing.builder()
        .id(UUID.randomUUID())
        .recipeId(recipeId)
        .content(content)
        .createdAt(clock.now())
        .build();
  }
}
