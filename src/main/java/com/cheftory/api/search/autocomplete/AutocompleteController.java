package com.cheftory.api.search.autocomplete;

import com.cheftory.api.search.exception.SearchException;
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

    @GetMapping("/search/autocomplete")
    public AutocompletesResponse getAutocomplete(
            @RequestParam("query") String query,
            @RequestParam(value = "scope", defaultValue = "RECIPE") AutocompleteScope scope)
            throws SearchException {
        List<Autocomplete> autocompletes = autocompleteService.autocomplete(scope, query);
        return AutocompletesResponse.from(autocompletes);
    }
}
