package com.cheftory.api.search.autocomplete;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AutocompletesResponse(
    @JsonProperty("autocompletes") List<Autocomplete> autocompletes) {

  private record Autocomplete(@JsonProperty("autocomplete") String autocomplete) {
    private static Autocomplete from(
        com.cheftory.api.search.autocomplete.Autocomplete autocomplete) {
      return new Autocomplete(autocomplete.getText());
    }
  }

  public static AutocompletesResponse from(
      List<com.cheftory.api.search.autocomplete.Autocomplete> autocompletes) {
    List<Autocomplete> list =
        autocompletes.stream().map(AutocompletesResponse.Autocomplete::from).toList();
    return new AutocompletesResponse(list);
  }
}
