package com.cheftory.api.recipeinfo.caption.entity;

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
public class RecipeCaption extends MarketScope {
  @Id private UUID id;

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Segment {
    private String text;
    private Double start;
    private Double end;
  }

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private List<Segment> segments;

  @Enumerated(EnumType.STRING)
  private LangCodeType langCode;

  private UUID recipeId;

  private LocalDateTime createdAt;

  public static RecipeCaption from(
      List<Segment> segments, LangCodeType langCodeType, UUID recipeId, Clock clock) {
    return RecipeCaption.builder()
        .id(UUID.randomUUID())
        .segments(segments)
        .langCode(langCodeType)
        .recipeId(recipeId)
        .createdAt(clock.now())
        .build();
  }
}
