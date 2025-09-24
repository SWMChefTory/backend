package com.cheftory.api.recipeinfo.identify;

import com.cheftory.api._common.Clock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.net.URI;
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
public class RecipeIdentify {
  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private URI url;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public static RecipeIdentify create(URI url, Clock clock) {
    return RecipeIdentify.builder().id(UUID.randomUUID()).url(url).createdAt(clock.now()).build();
  }
}
