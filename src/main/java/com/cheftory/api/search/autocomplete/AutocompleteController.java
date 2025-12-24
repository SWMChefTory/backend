package com.cheftory.api.search.autocomplete;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes/search/autocomplete")
@RequiredArgsConstructor
public class AutocompleteController {

  private final AutocompleteService autocompleteService;

  @GetMapping
  public AutocompletesResponse getAutocomplete(@RequestParam String query) {
    List<Autocomplete> autocompletes = autocompleteService.autocomplete(query);
    return AutocompletesResponse.from(autocompletes);
  }
}
