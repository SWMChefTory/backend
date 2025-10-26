package com.cheftory.api.recipeinfo.search.history;

import com.cheftory.api._common.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeSearchHistoryService {

  private final RecipeSearchHistoryRepository repository;
  private final RecipeSearchHistoryKeyGenerator keyGenerator;
  private final Clock clock;

  private static final int MAX_HISTORY = 10;
  private static final Duration TTL = Duration.ofDays(30);

  public void create(UUID userId, String searchText) {
    if (searchText == null || searchText.isBlank()) {
      return;
    }

    String key = keyGenerator.generate(userId);
    String trimmedText = searchText.trim();

    repository.save(key, trimmedText, clock);
    repository.removeOldEntries(key, MAX_HISTORY);
    repository.setExpire(key, TTL);
  }

  public List<String> get(UUID userId) {
    String key = keyGenerator.generate(userId);
    return repository.findRecent(key, MAX_HISTORY);
  }

  public void delete(UUID userId, String searchText) {
    if (searchText == null || searchText.isBlank()) {
      return;
    }

    String key = keyGenerator.generate(userId);
    String trimmedText = searchText.trim();

    repository.remove(key, trimmedText);
  }

  public void deleteAll(UUID userId) {
    String key = keyGenerator.generate(userId);
    repository.deleteAll(key);
  }
}
