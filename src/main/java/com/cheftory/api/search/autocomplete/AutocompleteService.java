package com.cheftory.api.search.autocomplete;

import com.cheftory.api.search.utils.SearchPageRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutocompleteService {
  private final AutocompleteRepository autocompleteRepository;

  public List<Autocomplete> autocomplete(String keyword) {
    return autocomplete(AutocompleteScope.RECIPE, keyword);
  }

  public List<Autocomplete> autocomplete(AutocompleteScope scope, String keyword) {
    Pageable pageable = SearchPageRequest.create(0);
    return autocompleteRepository.searchAutocomplete(scope, keyword, pageable).stream().toList();
  }
}
