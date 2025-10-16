package com.cheftory.api.recipeinfo.search.autocomplete;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecipeAutocompletesResponse(
    @JsonProperty("autocompletes") List<Autocomplete> autocompletes) {

  private record Autocomplete(@JsonProperty("autocomplete") String autocomplete) {
    private static Autocomplete from(RecipeAutocomplete autocomplete) {
      return new Autocomplete(autocomplete.getText());
    }
  }

  public static RecipeAutocompletesResponse from(List<RecipeAutocomplete> autocompletes) {
    List<Autocomplete> list = autocompletes.stream().map(Autocomplete::from).toList();
    return new RecipeAutocompletesResponse(list);
  }
}
