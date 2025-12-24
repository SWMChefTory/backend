package com.cheftory.api.search.history;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SearchHistoryKeyGenerator {

  private static final String USER_SEARCH_HISTORY_KEY = "recipeSearch:history:%s";

  public String generate(UUID userId) {
    return String.format(USER_SEARCH_HISTORY_KEY, userId);
  }
}
