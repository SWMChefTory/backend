package com.cheftory.api.search.indexing.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.UpdatedAtIdCursor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({IndexingCursorRepositoryImpl.class, Clock.class})
class IndexingCursorRepositoryTest extends DbContextTest {

    @Autowired
    private IndexingCursorRepository cursorRepository;

    @Autowired
    private IndexingCursorJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("커서가 없으면 초기값을 반환한다")
    void shouldReturnInitialCursorWhenCursorNotExists() {
        UpdatedAtIdCursor cursor = cursorRepository.load("search-query-upsert");

        assertThat(cursor.lastUpdatedAt()).isEqualTo(UpdatedAtIdCursor.DEFAULT_TIME);
        assertThat(cursor.lastId()).isEqualTo(UpdatedAtIdCursor.MIN_UUID);
    }

    @Test
    @DisplayName("커서를 저장하면 마지막 처리 지점을 다시 읽을 수 있다")
    void shouldLoadSavedCursor() {
        UpdatedAtIdCursor saved = new UpdatedAtIdCursor(
                LocalDateTime.of(2026, 3, 1, 12, 0, 30), UUID.fromString("00000000-0000-0000-0000-000000000123"));

        cursorRepository.save("search-query-upsert", saved);

        UpdatedAtIdCursor loaded = cursorRepository.load("search-query-upsert");
        assertThat(loaded).isEqualTo(saved);
    }

    @Test
    @DisplayName("같은 파이프라인 커서를 다시 저장하면 최신 값으로 갱신된다")
    void shouldUpdateCursorWhenSavedTwice() {
        cursorRepository.save(
                "search-query-delete",
                new UpdatedAtIdCursor(
                        LocalDateTime.of(2026, 3, 1, 10, 0, 0),
                        UUID.fromString("00000000-0000-0000-0000-000000000111")));

        UpdatedAtIdCursor updated = new UpdatedAtIdCursor(
                LocalDateTime.of(2026, 3, 1, 11, 0, 0), UUID.fromString("00000000-0000-0000-0000-000000000222"));
        cursorRepository.save("search-query-delete", updated);

        assertThat(cursorRepository.load("search-query-delete")).isEqualTo(updated);
    }
}
