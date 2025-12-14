package com.cheftory.api.recipeinfo.step.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeStep extends MarketScope {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Detail {
    private String text;
    private Double start;
  }

  @Id private UUID id;

  private Integer stepOrder;

  private String subtitle;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private List<Detail> details;

  private Double start;

  private UUID recipeId;

  private LocalDateTime createdAt;

  public static RecipeStep create(
      Integer stepOrder,
      String subtitle,
      List<Detail> details,
      Double start,
      UUID recipeId,
      Clock clock) {
    return RecipeStep.builder()
        .id(UUID.randomUUID())
        .stepOrder(stepOrder)
        .subtitle(subtitle)
        .details(details)
        .start(start)
        .recipeId(recipeId)
        .createdAt(clock.now())
        .build();
  }
}
