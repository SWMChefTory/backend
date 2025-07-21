package com.cheftory.api.recipeaccess.dto;

import com.cheftory.api.recipe.dto.FullRecipeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipeResponse {
  private FullRecipeInfo fullRecipeInfo;

  public static FullRecipeResponse of(FullRecipeInfo fullRecipeInfo) {
    return FullRecipeResponse.of(fullRecipeInfo);
  }
}
