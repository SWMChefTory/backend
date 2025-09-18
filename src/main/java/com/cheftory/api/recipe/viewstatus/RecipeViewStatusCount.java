package com.cheftory.api.recipe.viewstatus;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class RecipeViewStatusCount {
  private UUID categoryId;
  private Integer count;

  // JPA용 public 생성자 추가
  public RecipeViewStatusCount(UUID categoryId, Integer count) {
    this.categoryId = categoryId;
    this.count = count;
  }

  public static RecipeViewStatusCount of(UUID categoryId, Integer count) {
    return new RecipeViewStatusCount(categoryId, count);
  }
}
