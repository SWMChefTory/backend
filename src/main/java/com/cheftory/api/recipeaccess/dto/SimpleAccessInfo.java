package com.cheftory.api.recipeaccess.dto;

import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipeviewstate.dto.ViewStateInfo;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SimpleAccessInfo {
  private LocalDateTime viewedAt;
  private LocalDateTime createdAt;
  private Integer lastPlaySeconds;
  private RecipeOverview recipeOverview;
  public static SimpleAccessInfo of(
      ViewStateInfo viewStateInfo,
      RecipeOverview recipeOverview
  ) {
    return SimpleAccessInfo.builder()
        .viewedAt(viewStateInfo.getViewedAt())
        .createdAt(viewStateInfo.getCreatedAt())
        .lastPlaySeconds(viewStateInfo.getLastPlaySeconds())
        .recipeOverview(recipeOverview)
        .build();
  }
}
