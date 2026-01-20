package com.cheftory.api.search.history;

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
public class SearchHistoryService {

  private final SearchHistoryRepository repository;
  private final SearchHistoryKeyGenerator keyGenerator;
  private final Clock clock;

  private static final int MAX_HISTORY = 10;
  private static final Duration TTL = Duration.ofDays(30);

  public void create(UUID userId, String searchText) {
    create(userId, SearchHistoryScope.RECIPE, searchText);
  }

  public List<String> get(UUID userId) {
    return get(userId, SearchHistoryScope.RECIPE);
  }

  public void delete(UUID userId, String searchText) {
    delete(userId, SearchHistoryScope.RECIPE, searchText);
  }

  public void deleteAll(UUID userId) {
    deleteAll(userId, SearchHistoryScope.RECIPE);
  }

  public void create(UUID userId, SearchHistoryScope scope, String searchText) {
    if (searchText == null || searchText.isBlank()) {
      return;
    }

    String key = keyGenerator.generate(userId, scope);
    String trimmedText = searchText.trim();

    repository.save(key, trimmedText, clock);
    repository.removeOldEntries(key, MAX_HISTORY);
    repository.setExpire(key, TTL);
  }

  public List<String> get(UUID userId, SearchHistoryScope scope) {
    String key = keyGenerator.generate(userId, scope);
    return repository.findRecent(key, MAX_HISTORY);
  }

  public void delete(UUID userId, SearchHistoryScope scope, String searchText) {
    if (searchText == null || searchText.isBlank()) {
      return;
    }

    String key = keyGenerator.generate(userId, scope);
    String trimmedText = searchText.trim();

    repository.remove(key, trimmedText);
  }

  public void deleteAll(UUID userId, SearchHistoryScope scope) {
    String key = keyGenerator.generate(userId, scope);
    repository.deleteAll(key);
  }
}
