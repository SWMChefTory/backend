package com.cheftory.api.recipe.creation.identify.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.net.URI;
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
public class RecipeIdentify extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private URI url;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public static RecipeIdentify create(URI url, Clock clock) {
    return new RecipeIdentify(UUID.randomUUID(), url, clock.now());
  }
}
