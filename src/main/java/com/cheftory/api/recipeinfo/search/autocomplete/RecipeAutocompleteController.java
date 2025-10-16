package com.cheftory.api.recipeinfo.search.autocomplete;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes/search/autocomplete")
@RequiredArgsConstructor
public class RecipeAutocompleteController {

  private final RecipeAutocompleteService recipeAutocompleteService;

  @GetMapping
  public RecipeAutocompletesResponse getAutocomplete(@RequestParam String query) {
    List<RecipeAutocomplete> autocompletes = recipeAutocompleteService.autocomplete(query);
    return RecipeAutocompletesResponse.from(autocompletes);
  }
}
