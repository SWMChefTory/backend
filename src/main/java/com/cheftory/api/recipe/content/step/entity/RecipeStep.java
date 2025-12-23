package com.cheftory.api.recipe.content.step.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeStep extends MarketScope {

  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public static class Detail {
    private String text;
    private Double start;

    public static Detail of(String text, Double start) {
      return new Detail(text, start);
    }
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

    return new RecipeStep(
        UUID.randomUUID(), stepOrder, subtitle, details, start, recipeId, clock.now());
  }
}
