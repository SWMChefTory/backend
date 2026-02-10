package com.cheftory.api.search.history;

import com.cheftory.api._common.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 검색 히스토리 서비스.
 *
 * <p>사용자의 검색 히스토리를 관리합니다.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchHistoryService {

    /** 검색 히스토리 Redis 리포지토리. */
    private final SearchHistoryRepository repository;

    /** 검색 히스토리 키 생성기. */
    private final SearchHistoryKeyGenerator keyGenerator;

    /** 클럭. */
    private final Clock clock;

    /** 최대 검색 히스토리 개수. */
    private static final int MAX_HISTORY = 10;

    /** 검색 히스토리 만료 기간. */
    private static final Duration TTL = Duration.ofDays(30);

    /**
     * 검색 히스토리를 생성합니다 (기본 범위: RECIPE).
     *
     * @param userId 사용자 ID
     * @param searchText 검색어
     */
    public void create(UUID userId, String searchText) {
        create(userId, SearchHistoryScope.RECIPE, searchText);
    }

    /**
     * 검색 히스토리를 조회합니다 (기본 범위: RECIPE).
     *
     * @param userId 사용자 ID
     * @return 검색어 목록
     */
    public List<String> get(UUID userId) {
        return get(userId, SearchHistoryScope.RECIPE);
    }

    /**
     * 특정 검색 히스토리를 삭제합니다 (기본 범위: RECIPE).
     *
     * @param userId 사용자 ID
     * @param searchText 검색어
     */
    public void delete(UUID userId, String searchText) {
        delete(userId, SearchHistoryScope.RECIPE, searchText);
    }

    /**
     * 모든 검색 히스토리를 삭제합니다 (기본 범위: RECIPE).
     *
     * @param userId 사용자 ID
     */
    public void deleteAll(UUID userId) {
        deleteAll(userId, SearchHistoryScope.RECIPE);
    }

    /**
     * 검색 히스토리를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위
     * @param searchText 검색어
     */
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

    /**
     * 검색 히스토리를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위
     * @return 검색어 목록
     */
    public List<String> get(UUID userId, SearchHistoryScope scope) {
        String key = keyGenerator.generate(userId, scope);
        return repository.findRecent(key, MAX_HISTORY);
    }

    /**
     * 특정 검색 히스토리를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위
     * @param searchText 검색어
     */
    public void delete(UUID userId, SearchHistoryScope scope, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return;
        }

        String key = keyGenerator.generate(userId, scope);
        String trimmedText = searchText.trim();

        repository.remove(key, trimmedText);
    }

    /**
     * 모든 검색 히스토리를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위
     */
    public void deleteAll(UUID userId, SearchHistoryScope scope) {
        String key = keyGenerator.generate(userId, scope);
        repository.deleteAll(key);
    }
}
