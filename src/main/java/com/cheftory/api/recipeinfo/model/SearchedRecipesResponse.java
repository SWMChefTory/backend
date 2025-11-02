package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;

public record SearchedRecipesResponse(
    @JsonProperty("searched_recipes") List<RecipeOverviewResponse> searchedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static SearchedRecipesResponse from(Page<RecipeOverview> recipes) {
    List<RecipeOverviewResponse> responses = recipes.stream().map(RecipeOverviewResponse::from).toList();
    return new SearchedRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }
}
