package com.cheftory.api.search.autocomplete;

import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutocompleteService {
    private static final int DEFAULT_LIMIT = 10;
    private final AutocompleteRepository autocompleteRepository;

    public List<Autocomplete> autocomplete(AutocompleteScope scope, String keyword) throws SearchException {
        return autocompleteRepository.searchAutocomplete(scope, keyword, DEFAULT_LIMIT);
    }
}
