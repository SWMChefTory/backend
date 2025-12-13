package com.cheftory.api.recipeinfo.search.autocomplete;

import com.cheftory.api.recipeinfo.search.utils.RecipeSearchPageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeAutocompleteService {
  private final RecipeAutocompleteRepository recipeAutocompleteRepository;

  public List<RecipeAutocomplete> autocomplete(String keyword) {
    Pageable pageable = RecipeSearchPageRequest.create(0);
    return recipeAutocompleteRepository.searchAutocomplete(keyword, pageable).stream().toList();
  }
}
