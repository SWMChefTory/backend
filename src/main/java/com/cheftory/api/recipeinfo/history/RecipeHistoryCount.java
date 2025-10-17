package com.cheftory.api.recipeinfo.history;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecipeHistoryCount {
  private UUID categoryId;
  private Integer count;

  // JPA용 public 생성자 추가
  public RecipeHistoryCount(UUID categoryId, Integer count) {
    this.categoryId = categoryId;
    this.count = count;
  }

  public static RecipeHistoryCount of(UUID categoryId, Integer count) {
    return new RecipeHistoryCount(categoryId, count);
  }
}
