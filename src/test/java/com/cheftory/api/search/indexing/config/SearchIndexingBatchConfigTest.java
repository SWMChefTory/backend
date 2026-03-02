package com.cheftory.api.search.indexing.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.cursor.UpdatedAtIdCursor;
import com.cheftory.api.search.indexing.autocomplete.AutocompleteAggregateRow;
import com.cheftory.api.search.indexing.query.SearchQueryDeleteRow;
import com.cheftory.api.search.indexing.query.SearchQueryUpsertRow;
import com.cheftory.api.search.indexing.support.BulkIndexPayload;
import com.cheftory.api.search.indexing.support.IndexingCursorRepository;
import com.cheftory.api.search.indexing.support.SearchIndexingBulkClient;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchIndexingBatchConfig 테스트")
class SearchIndexingBatchConfigTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private SearchIndexingBulkClient bulkClient;

    @Mock
    private IndexingCursorRepository cursorRepository;

    private SearchIndexingBatchConfig config;

    @BeforeEach
    void setUp() {
        config = new SearchIndexingBatchConfig(dataSource, bulkClient, cursorRepository, new ObjectMapper());
        ReflectionTestUtils.setField(config, "batchSize", 250);
        ReflectionTestUtils.setField(config, "timezone", "Asia/Seoul");
    }

    @Test
    @DisplayName("searchQueryUpsertWriter는 payload를 인덱싱하고 마지막 커서를 저장한다")
    void shouldBulkIndexAndSaveCursorForUpsertWriter() throws Exception {
        ItemWriter<SearchQueryUpsertRow> writer = config.searchQueryUpsertWriter();
        SearchQueryUpsertRow first = new SearchQueryUpsertRow(
                "00000000-0000-0000-0000-000000000101",
                "kr",
                "김치찌개",
                "채널A",
                "recipe",
                "2인분",
                List.of("김치", "돼지고기"),
                List.of("매운맛", "국물"),
                LocalDateTime.of(2026, 3, 1, 10, 0, 0),
                LocalDateTime.of(2026, 3, 1, 10, 5, 0));
        SearchQueryUpsertRow second = new SearchQueryUpsertRow(
                "00000000-0000-0000-0000-000000000202",
                "kr",
                "된장찌개",
                "채널B",
                "recipe",
                "3인분",
                List.of("된장", "두부"),
                List.of("구수함"),
                LocalDateTime.of(2026, 3, 1, 11, 0, 0),
                LocalDateTime.of(2026, 3, 1, 11, 10, 0));

        writer.write(Chunk.of(first, second));

        ArgumentCaptor<List<BulkIndexPayload>> payloadsCaptor = ArgumentCaptor.forClass(List.class);
        verify(bulkClient).bulkIndex(eq("search_query"), payloadsCaptor.capture());
        List<BulkIndexPayload> payloads = payloadsCaptor.getValue();
        assertThat(payloads).hasSize(2);
        assertThat(payloads.get(0).id()).isEqualTo(first.id());

        @SuppressWarnings("unchecked")
        Map<String, Object> document = (Map<String, Object>) payloads.get(0).document();
        assertThat(document.get("title")).isEqualTo("김치찌개");
        assertThat(document.get("channel_title")).isEqualTo("채널A");
        assertThat(document.get("ingredients")).isEqualTo(List.of("김치", "돼지고기"));
        assertThat(document.get("keywords")).isEqualTo(List.of("매운맛", "국물"));
        assertThat(document.get("created_at"))
                .isEqualTo(first.createdAt()
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                        .toEpochMilli());
        assertThat(document.get("updated_at"))
                .isEqualTo(first.updatedAt()
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                        .toEpochMilli());

        ArgumentCaptor<UpdatedAtIdCursor> cursorCaptor = ArgumentCaptor.forClass(UpdatedAtIdCursor.class);
        verify(cursorRepository).save(eq("search-query-upsert"), cursorCaptor.capture());
        assertThat(cursorCaptor.getValue().lastUpdatedAt()).isEqualTo(second.updatedAt());
        assertThat(cursorCaptor.getValue().lastId()).isEqualTo(UUID.fromString(second.id()));
    }

    @Test
    @DisplayName("searchQueryDeleteWriter는 ID 삭제와 마지막 커서 저장을 수행한다")
    void shouldBulkDeleteAndSaveCursorForDeleteWriter() throws Exception {
        ItemWriter<SearchQueryDeleteRow> writer = config.searchQueryDeleteWriter();
        SearchQueryDeleteRow first =
                new SearchQueryDeleteRow("00000000-0000-0000-0000-000000000011", LocalDateTime.of(2026, 3, 1, 9, 0));
        SearchQueryDeleteRow second =
                new SearchQueryDeleteRow("00000000-0000-0000-0000-000000000022", LocalDateTime.of(2026, 3, 1, 9, 30));

        writer.write(Chunk.of(first, second));

        verify(bulkClient).bulkDelete("search_query", List.of(first.id(), second.id()));

        ArgumentCaptor<UpdatedAtIdCursor> cursorCaptor = ArgumentCaptor.forClass(UpdatedAtIdCursor.class);
        verify(cursorRepository).save(eq("search-query-delete"), cursorCaptor.capture());
        assertThat(cursorCaptor.getValue().lastUpdatedAt()).isEqualTo(second.updatedAt());
        assertThat(cursorCaptor.getValue().lastId()).isEqualTo(UUID.fromString(second.id()));
    }

    @Test
    @DisplayName("autocompleteWriter는 market/scope를 소문자로 정규화해서 인덱싱한다")
    void shouldNormalizeMarketAndScopeForAutocompleteWriter() throws Exception {
        ItemWriter<AutocompleteAggregateRow> writer = config.autocompleteWriter();
        AutocompleteAggregateRow row = new AutocompleteAggregateRow("KR", " 김치찌개 ", "RECIPE", 25);

        writer.write(Chunk.of(row));

        ArgumentCaptor<List<BulkIndexPayload>> payloadsCaptor = ArgumentCaptor.forClass(List.class);
        verify(bulkClient).bulkIndex(eq("autocomplete"), payloadsCaptor.capture());
        BulkIndexPayload payload = payloadsCaptor.getValue().getFirst();

        assertThat(payload.id()).isEqualTo("kr:recipe:김치찌개");
        @SuppressWarnings("unchecked")
        Map<String, Object> document = (Map<String, Object>) payload.document();
        assertThat(document.get("market")).isEqualTo("kr");
        assertThat(document.get("scope")).isEqualTo("recipe");
        assertThat(document.get("text")).isEqualTo("김치찌개");
        assertThat(document.get("count")).isEqualTo(25);
    }

    @Test
    @DisplayName("searchQueryUpsertWriter는 마지막 id가 UUID가 아니면 예외를 던지고 커서를 저장하지 않는다")
    void shouldThrowWhenLastUpsertIdIsInvalidUuid() {
        ItemWriter<SearchQueryUpsertRow> writer = config.searchQueryUpsertWriter();
        SearchQueryUpsertRow row = new SearchQueryUpsertRow(
                "not-a-uuid",
                "kr",
                "테스트",
                "채널",
                "recipe",
                "1인분",
                List.of(),
                List.of(),
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 1, 10, 1));

        assertThatThrownBy(() -> writer.write(Chunk.of(row))).isInstanceOf(IllegalStateException.class);
        verify(cursorRepository, never()).save(eq("search-query-upsert"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("searchQueryUpsertReader는 커서와 upperBound를 파라미터로 설정한다")
    void shouldBuildReaderWithCursorAndUpperBoundForUpsert() throws Exception {
        UpdatedAtIdCursor cursor = new UpdatedAtIdCursor(
                LocalDateTime.of(2026, 3, 1, 8, 0), UUID.fromString("00000000-0000-0000-0000-000000000333"));
        when(cursorRepository.load("search-query-upsert")).thenReturn(cursor);

        JdbcPagingItemReader<SearchQueryUpsertRow> reader = config.searchQueryUpsertReader("2026-03-01T12:00:00");

        assertThat(reader.getPageSize()).isEqualTo(250);
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) ReflectionTestUtils.getField(reader, "parameterValues");
        assertThat(params.get("last_updated_at")).isEqualTo(Timestamp.valueOf(cursor.lastUpdatedAt()));
        assertThat(params.get("last_id")).isEqualTo(cursor.lastId().toString());
        assertThat(params.get("upper_bound_updated_at")).isEqualTo(Timestamp.valueOf("2026-03-01 12:00:00"));
    }

    @Test
    @DisplayName("searchQueryDeleteReader는 커서와 upperBound를 파라미터로 설정한다")
    void shouldBuildReaderWithCursorAndUpperBoundForDelete() throws Exception {
        UpdatedAtIdCursor cursor = new UpdatedAtIdCursor(
                LocalDateTime.of(2026, 3, 1, 7, 30), UUID.fromString("00000000-0000-0000-0000-000000000444"));
        when(cursorRepository.load("search-query-delete")).thenReturn(cursor);

        JdbcPagingItemReader<SearchQueryDeleteRow> reader = config.searchQueryDeleteReader("2026-03-01T12:30:00");

        assertThat(reader.getPageSize()).isEqualTo(250);
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) ReflectionTestUtils.getField(reader, "parameterValues");
        assertThat(params.get("last_updated_at")).isEqualTo(Timestamp.valueOf(cursor.lastUpdatedAt()));
        assertThat(params.get("last_id")).isEqualTo(cursor.lastId().toString());
        assertThat(params.get("upper_bound_updated_at")).isEqualTo(Timestamp.valueOf("2026-03-01 12:30:00"));
    }

    @Test
    @DisplayName("reader 생성 시 upperBound 포맷이 잘못되면 예외를 던진다")
    void shouldThrowWhenUpperBoundUpdatedAtIsInvalid() {
        when(cursorRepository.load("search-query-upsert")).thenReturn(UpdatedAtIdCursor.initial());

        assertThatThrownBy(() -> config.searchQueryUpsertReader("invalid-date")).isInstanceOf(RuntimeException.class);
    }
}
