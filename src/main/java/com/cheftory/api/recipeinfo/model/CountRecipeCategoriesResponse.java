package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record CountRecipeCategoriesResponse(
    @JsonProperty("categories") List<CountRecipeCategoryResponse> categories) {
  public static CountRecipeCategoriesResponse from(List<CountRecipeCategory> categories) {
    List<CountRecipeCategoryResponse> responses =
        categories.stream().map(CountRecipeCategoryResponse::from).toList();
    return new CountRecipeCategoriesResponse(responses);
  }

  public record CountRecipeCategoryResponse(
      @JsonProperty("category_id") UUID categoryId,
      @JsonProperty("count") Integer count,
      @JsonProperty("name") String name) {
    public static CountRecipeCategoryResponse from(CountRecipeCategory category) {
      return new CountRecipeCategoryResponse(
          category.getCategory().getId(), category.getCount(), category.getCategory().getName());
    }
  }
}
