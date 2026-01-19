package com.cheftory.api.search.autocomplete;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AutocompleteController {

  private final AutocompleteService autocompleteService;

  @Deprecated(forRemoval = true, since = "v1")
  @GetMapping("/recipes/search/autocomplete")
  public AutocompletesResponse getRecipeAutocomplete(@RequestParam("query") String query) {
    List<Autocomplete> autocompletes = autocompleteService.autocomplete(query);
    return AutocompletesResponse.from(autocompletes);
  }

  @GetMapping("/search/autocomplete")
  public AutocompletesResponse getAutocomplete(
      @RequestParam("query") String query,
      @RequestParam(value = "scope", defaultValue = "RECIPE") AutocompleteScope scope) {
    List<Autocomplete> autocompletes = autocompleteService.autocomplete(scope, query);
    return AutocompletesResponse.from(autocompletes);
  }
}
