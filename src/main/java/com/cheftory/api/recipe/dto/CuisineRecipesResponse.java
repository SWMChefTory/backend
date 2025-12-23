package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record CuisineRecipesResponse(
    @JsonProperty("cuisine_recipes") List<RecipeOverviewResponse> searchedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static CuisineRecipesResponse from(Page<RecipeOverview> recipes) {
    List<RecipeOverviewResponse> responses =
        recipes.stream().map(RecipeOverviewResponse::of).toList();
    return new CuisineRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }
}
