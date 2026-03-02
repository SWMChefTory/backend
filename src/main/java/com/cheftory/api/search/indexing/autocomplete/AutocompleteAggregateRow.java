package com.cheftory.api.search.indexing.autocomplete;

public record AutocompleteAggregateRow(String market, String text, String scope, int count) {}
