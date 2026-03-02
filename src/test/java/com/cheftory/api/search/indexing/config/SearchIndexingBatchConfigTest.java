package com.cheftory.api.search.indexing.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cheftory.api._common.cursor.UpdatedAtIdCursor;
import com.cheftory.api.search.indexing.autocomplete.AutocompleteAggregateRow;
import com.cheftory.api.search.indexing.query.SearchQueryDeleteRow;
import com.cheftory.api.search.indexing.query.SearchQueryUpsertRow;
import com.cheftory.api.search.indexing.support.BulkIndexPayload;
import com.cheftory.api.search.indexing.support.IndexingCursorJpaRepository;
import com.cheftory.api.search.indexing.support.IndexingCursorRepository;
import com.cheftory.api.search.indexing.support.SearchIndexingBulkClient;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.PagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@DisplayName("SearchIndexingBatchConfig 테스트")
public class SearchIndexingBatchConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private SearchIndexingBulkClient bulkClient;

    @Autowired
    private IndexingCursorRepository cursorRepository;

    @Autowired
    private IndexingCursorJpaRepository cursorJpaRepository;

    private SearchIndexingBatchConfig config;

    @BeforeEach
    void setUp() {
        registerMysqlCompatibility();
        clearAllTables();
        reset(bulkClient);
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

        UpdatedAtIdCursor saved = cursorRepository.load("search-query-upsert");
        assertThat(saved.lastUpdatedAt()).isEqualTo(second.updatedAt());
        assertThat(saved.lastId()).isEqualTo(UUID.fromString(second.id()));
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

        UpdatedAtIdCursor saved = cursorRepository.load("search-query-delete");
        assertThat(saved.lastUpdatedAt()).isEqualTo(second.updatedAt());
        assertThat(saved.lastId()).isEqualTo(UUID.fromString(second.id()));
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
    @DisplayName("writer는 빈 청크 입력 시 외부 호출 없이 종료한다")
    void shouldNoOpWhenChunkIsEmpty() throws Exception {
        config.searchQueryUpsertWriter().write(Chunk.of());
        config.searchQueryDeleteWriter().write(Chunk.of());
        config.autocompleteWriter().write(Chunk.of());

        verifyNoInteractions(bulkClient);
        assertThat(cursorRepository.load("search-query-upsert")).isEqualTo(UpdatedAtIdCursor.initial());
        assertThat(cursorRepository.load("search-query-delete")).isEqualTo(UpdatedAtIdCursor.initial());
    }

    @Test
    @DisplayName("searchQueryUpsertReader는 커서와 upperBound를 파라미터로 설정한다")
    void shouldBuildReaderWithCursorAndUpperBoundForUpsert() throws Exception {
        UpdatedAtIdCursor cursor = new UpdatedAtIdCursor(
                LocalDateTime.of(2026, 3, 1, 8, 0), UUID.fromString("00000000-0000-0000-0000-000000000333"));
        cursorRepository.save("search-query-upsert", cursor);

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
        cursorRepository.save("search-query-delete", cursor);

        JdbcPagingItemReader<SearchQueryDeleteRow> reader = config.searchQueryDeleteReader("2026-03-01T12:30:00");

        assertThat(reader.getPageSize()).isEqualTo(250);
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) ReflectionTestUtils.getField(reader, "parameterValues");
        assertThat(params.get("last_updated_at")).isEqualTo(Timestamp.valueOf(cursor.lastUpdatedAt()));
        assertThat(params.get("last_id")).isEqualTo(cursor.lastId().toString());
        assertThat(params.get("upper_bound_updated_at")).isEqualTo(Timestamp.valueOf("2026-03-01 12:30:00"));
    }

    @Test
    @DisplayName("upsert reader 쿼리는 SUCCESS 필터와 updated_at,id 오름차순 정렬을 사용한다")
    void shouldUseExpectedFilterAndSortForUpsertReaderQuery() throws Exception {
        JdbcPagingItemReader<SearchQueryUpsertRow> reader = config.searchQueryUpsertReader("2026-03-01T12:30:00");
        PagingQueryProvider queryProvider = queryProviderOf(reader);
        String firstPageQuery = queryProvider.generateFirstPageQuery(50);
        String remainingPagesQuery = queryProvider.generateRemainingPagesQuery(50);

        assertThat(queryProvider.getSortKeys()).containsEntry("updated_at", Order.ASCENDING);
        assertThat(queryProvider.getSortKeys().keySet()).anyMatch(key -> key.equals("id") || key.equals("sort_id"));
        assertThat(firstPageQuery).contains("r.recipe_status = 'SUCCESS'");
        assertThat(firstPageQuery.toLowerCase())
                .contains("order by updated_at asc")
                .containsAnyOf("id asc", "sort_id asc");
        assertThat(remainingPagesQuery).contains(":last_updated_at");
        assertThat(remainingPagesQuery).contains(":last_id");
    }

    @Test
    @DisplayName("delete reader 쿼리는 BLOCKED/FAILED 필터와 updated_at,id 오름차순 정렬을 사용한다")
    void shouldUseExpectedFilterAndSortForDeleteReaderQuery() throws Exception {
        JdbcPagingItemReader<SearchQueryDeleteRow> reader = config.searchQueryDeleteReader("2026-03-01T12:30:00");
        PagingQueryProvider queryProvider = queryProviderOf(reader);
        String firstPageQuery = queryProvider.generateFirstPageQuery(50);
        String remainingPagesQuery = queryProvider.generateRemainingPagesQuery(50);

        assertThat(queryProvider.getSortKeys()).containsEntry("updated_at", Order.ASCENDING);
        assertThat(queryProvider.getSortKeys().keySet()).anyMatch(key -> key.equals("id") || key.equals("sort_id"));
        assertThat(firstPageQuery).contains("r.recipe_status IN ('BLOCKED', 'FAILED')");
        assertThat(firstPageQuery.toLowerCase())
                .contains("order by updated_at asc")
                .containsAnyOf("id asc", "sort_id asc");
        assertThat(remainingPagesQuery).contains(":last_updated_at");
        assertThat(remainingPagesQuery).contains(":last_id");
    }

    @Test
    @DisplayName("autocomplete reader 쿼리는 SUCCESS 집계와 market,text 정렬을 사용한다")
    void shouldUseExpectedSortForAutocompleteReaderQuery() throws Exception {
        JdbcCursorItemReader<AutocompleteAggregateRow> reader = config.autocompleteReader();
        String sql = (String) ReflectionTestUtils.getField(reader, "sql");

        assertThat(sql).contains("r.recipe_status = 'SUCCESS'");
        assertThat(sql).containsIgnoringCase("GROUP BY all_text.market, all_text.text");
        assertThat(sql).containsIgnoringCase("ORDER BY all_text.market ASC, all_text.text ASC");
    }

    @Test
    @DisplayName("searchQueryDeleteReader는 updated_at,id 오름차순으로 페이징 조회한다")
    void shouldReadDeleteRowsInUpdatedAtAndIdAscendingOrder() throws Exception {
        UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID thirdId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        LocalDateTime timeA = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime timeB = LocalDateTime.of(2026, 3, 1, 11, 0, 0);
        insertRecipe(firstId, "BLOCKED", timeA);
        insertRecipe(secondId, "FAILED", timeA);
        insertRecipe(thirdId, "FAILED", timeB);
        insertRecipe(UUID.fromString("00000000-0000-0000-0000-000000000010"), "SUCCESS", timeA);

        JdbcPagingItemReader<SearchQueryDeleteRow> reader = config.searchQueryDeleteReader("2026-03-01T12:00:00");
        List<SearchQueryDeleteRow> rows = readFirstN(reader, 3);

        assertThat(rows)
                .extracting(SearchQueryDeleteRow::id)
                .containsExactly(firstId.toString(), secondId.toString(), thirdId.toString());
        assertThat(rows).extracting(SearchQueryDeleteRow::updatedAt).containsExactly(timeA, timeA, timeB);
    }

    @Test
    @DisplayName("searchQueryDeleteReader는 cursor/upperBound 조건으로 읽을 대상을 제한한다")
    void shouldFilterDeleteRowsByCursorAndUpperBound() throws Exception {
        UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000050");
        LocalDateTime cursorTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        cursorRepository.save("search-query-delete", new UpdatedAtIdCursor(cursorTime, cursorId));

        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000010"),
                "BLOCKED",
                LocalDateTime.of(2026, 3, 1, 9, 59, 59));
        insertRecipe(UUID.fromString("00000000-0000-0000-0000-000000000049"), "FAILED", cursorTime);
        insertRecipe(UUID.fromString("00000000-0000-0000-0000-000000000051"), "BLOCKED", cursorTime);
        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000060"),
                "FAILED",
                LocalDateTime.of(2026, 3, 1, 10, 10, 0));
        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000070"),
                "FAILED",
                LocalDateTime.of(2026, 3, 1, 10, 31, 0));

        JdbcPagingItemReader<SearchQueryDeleteRow> reader = config.searchQueryDeleteReader("2026-03-01T10:30:00");
        List<SearchQueryDeleteRow> rows = readFirstN(reader, 2);

        assertThat(rows)
                .extracting(SearchQueryDeleteRow::id)
                .containsExactly("00000000-0000-0000-0000-000000000051", "00000000-0000-0000-0000-000000000060");
    }

    @Test
    @DisplayName("searchQueryUpsertReader는 SUCCESS만 updated_at,id 오름차순으로 읽는다")
    void shouldReadUpsertRowsInUpdatedAtAndIdAscendingOrder() throws Exception {
        UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000102");
        UUID thirdId = UUID.fromString("00000000-0000-0000-0000-000000000103");
        LocalDateTime timeA = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        LocalDateTime timeB = LocalDateTime.of(2026, 3, 1, 11, 0, 0);
        insertRecipe(firstId, "SUCCESS", timeA);
        insertRecipe(secondId, "SUCCESS", timeA);
        insertRecipe(thirdId, "SUCCESS", timeB);
        insertRecipe(UUID.fromString("00000000-0000-0000-0000-000000000199"), "FAILED", timeA);

        insertRecipeDetailMeta(firstId, 2, timeA.minusMinutes(1));
        insertRecipeIngredient(firstId, "김치", timeA.minusMinutes(1));
        insertRecipeIngredient(firstId, "돼지고기", timeA.minusMinutes(1));
        insertRecipeTag(firstId, "매운맛", timeA.minusMinutes(1));
        insertRecipeTag(firstId, "국물", timeA.minusMinutes(1));

        JdbcPagingItemReader<SearchQueryUpsertRow> reader = config.searchQueryUpsertReader("2026-03-01T12:00:00");
        List<SearchQueryUpsertRow> rows = readFirstN(reader, 3);

        assertThat(rows)
                .extracting(SearchQueryUpsertRow::id)
                .containsExactly(firstId.toString(), secondId.toString(), thirdId.toString());
        assertThat(rows.getFirst().market()).isEqualTo("kr");
        assertThat(rows.getFirst().scope()).isEqualTo("recipe");
        assertThat(rows.getFirst().servingsText()).isEqualTo("2인분");
        assertThat(rows.getFirst().ingredients()).containsExactlyInAnyOrder("김치", "돼지고기");
        assertThat(rows.getFirst().keywords()).containsExactlyInAnyOrder("매운맛", "국물");
    }

    @Test
    @DisplayName("searchQueryUpsertReader는 cursor/upperBound 조건으로 읽을 대상을 제한한다")
    void shouldFilterUpsertRowsByCursorAndUpperBound() throws Exception {
        UUID cursorId = UUID.fromString("00000000-0000-0000-0000-000000000500");
        LocalDateTime cursorTime = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        cursorRepository.save("search-query-upsert", new UpdatedAtIdCursor(cursorTime, cursorId));

        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000400"),
                "SUCCESS",
                LocalDateTime.of(2026, 3, 1, 9, 59, 0));
        insertRecipe(UUID.fromString("00000000-0000-0000-0000-000000000499"), "SUCCESS", cursorTime);
        insertRecipe(UUID.fromString("00000000-0000-0000-0000-000000000501"), "SUCCESS", cursorTime);
        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000510"),
                "SUCCESS",
                LocalDateTime.of(2026, 3, 1, 10, 5, 0));
        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000520"),
                "SUCCESS",
                LocalDateTime.of(2026, 3, 1, 10, 31, 0));
        insertRecipe(
                UUID.fromString("00000000-0000-0000-0000-000000000530"),
                "FAILED",
                LocalDateTime.of(2026, 3, 1, 10, 5, 0));

        JdbcPagingItemReader<SearchQueryUpsertRow> reader = config.searchQueryUpsertReader("2026-03-01T10:30:00");
        List<SearchQueryUpsertRow> rows = readFirstN(reader, 2);

        assertThat(rows)
                .extracting(SearchQueryUpsertRow::id)
                .containsExactly("00000000-0000-0000-0000-000000000501", "00000000-0000-0000-0000-000000000510");
    }

    @Test
    @DisplayName("autocompleteReader는 SUCCESS 데이터만 집계하고 market,text 오름차순으로 반환한다")
    void shouldReadAutocompleteAggregatesFromSuccessRecipesOnly() throws Exception {
        UUID successA = UUID.fromString("00000000-0000-0000-0000-000000000701");
        UUID successB = UUID.fromString("00000000-0000-0000-0000-000000000702");
        UUID blocked = UUID.fromString("00000000-0000-0000-0000-000000000703");
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 12, 0, 0);
        insertRecipe(successA, "SUCCESS", now);
        insertRecipe(successB, "SUCCESS", now.plusMinutes(1));
        insertRecipe(blocked, "BLOCKED", now.plusMinutes(2));

        insertRecipeIngredient(successA, "김치", now);
        insertRecipeIngredient(successB, "김치", now);
        insertRecipeIngredient(blocked, "차단재료", now);
        insertRecipeTag(successA, "매운맛", now);
        insertRecipeDetailMeta(successA, 2, now);

        JdbcCursorItemReader<AutocompleteAggregateRow> reader = config.autocompleteReader();
        List<AutocompleteAggregateRow> rows = readAll(reader);

        assertThat(rows).extracting(AutocompleteAggregateRow::scope).containsOnly("recipe");
        assertThat(rows).extracting(AutocompleteAggregateRow::text).doesNotContain("차단재료");
        assertThat(rows).anySatisfy(row -> {
            assertThat(row.market()).isEqualTo("kr");
            assertThat(row.text()).isEqualTo("김치");
            assertThat(row.count()).isEqualTo(2);
        });
        assertThat(rows.stream().map(row -> row.market() + ":" + row.text()).toList())
                .isSorted();
    }

    @Test
    @DisplayName("autocompleteReader는 source별 가중치(1.0,0.5,0.1)를 합산해 반올림 집계한다")
    void shouldAggregateAutocompleteByWeightAndRounding() throws Exception {
        UUID first = UUID.fromString("00000000-0000-0000-0000-000000000801");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000802");
        UUID third = UUID.fromString("00000000-0000-0000-0000-000000000803");
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 12, 0, 0);
        insertRecipe(first, "SUCCESS", now);
        insertRecipe(second, "SUCCESS", now.plusMinutes(1));
        insertRecipe(third, "SUCCESS", now.plusMinutes(2));

        // channel_title 0.5 가중치가 2건이면 round(1.0)=1 이어야 한다.
        insertRecipeYoutubeMeta(first, "title-a", "half-weight", now);
        insertRecipeYoutubeMeta(second, "title-b", "half-weight", now);
        // ingredients/tag는 1.0 가중치가 누적되어 2가 된다.
        insertRecipeIngredient(first, "full-weight", now);
        insertRecipeTag(second, "full-weight", now);
        // servings는 0.1 가중치라 3건이면 round(0.3)=0 이어야 한다.
        insertRecipeDetailMeta(first, 9, now);
        insertRecipeDetailMeta(second, 9, now);
        insertRecipeDetailMeta(third, 9, now);

        JdbcCursorItemReader<AutocompleteAggregateRow> reader = config.autocompleteReader();
        List<AutocompleteAggregateRow> rows = readAll(reader);

        Map<String, Integer> countByText = rows.stream()
                .collect(java.util.stream.Collectors.toMap(
                        AutocompleteAggregateRow::text, AutocompleteAggregateRow::count, (left, right) -> left));
        assertThat(countByText.get("half-weight")).isEqualTo(1);
        assertThat(countByText.get("full-weight")).isEqualTo(2);
        assertThat(countByText.get("9인분")).isEqualTo(0);
    }

    @Test
    @DisplayName("autocompleteReader는 youtube 메타가 없는 SUCCESS 레시피에서 title/channel 경로를 만들지 않는다")
    void shouldNotCreateTitleChannelRowsWhenYoutubeMetaMissing() throws Exception {
        UUID recipeId = UUID.fromString("00000000-0000-0000-0000-000000000811");
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 13, 0, 0);
        insertRecipe(recipeId, "SUCCESS", now);

        // youtube 메타는 넣지 않고 재료/태그/인분만 넣는다.
        insertRecipeIngredient(recipeId, "only-ingredient", now);
        insertRecipeTag(recipeId, "only-tag", now);
        insertRecipeDetailMeta(recipeId, 4, now);

        JdbcCursorItemReader<AutocompleteAggregateRow> reader = config.autocompleteReader();
        List<AutocompleteAggregateRow> rows = readAll(reader);

        assertThat(rows)
                .extracting(AutocompleteAggregateRow::text)
                .contains("only-ingredient", "only-tag", "4인분")
                .doesNotContain("title", "channel_title");
    }

    @Test
    @DisplayName("autocompleteReader는 다건 집계를 모두 정렬 순서대로 읽는다")
    void shouldReadAllAutocompleteRowsInOrder() throws Exception {
        ReflectionTestUtils.setField(config, "batchSize", 2);

        UUID recipeA = UUID.fromString("00000000-0000-0000-0000-000000000821");
        UUID recipeB = UUID.fromString("00000000-0000-0000-0000-000000000822");
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 14, 0, 0);
        insertRecipe(recipeA, "SUCCESS", now);
        insertRecipe(recipeB, "SUCCESS", now.plusMinutes(1));
        insertRecipeIngredient(recipeA, "aa", now);
        insertRecipeIngredient(recipeA, "ab", now);
        insertRecipeIngredient(recipeA, "ac", now);
        insertRecipeIngredient(recipeB, "ba", now);
        insertRecipeIngredient(recipeB, "bb", now);

        JdbcCursorItemReader<AutocompleteAggregateRow> reader = config.autocompleteReader();
        List<AutocompleteAggregateRow> rows = readAll(reader);
        List<String> texts = rows.stream()
                .map(AutocompleteAggregateRow::text)
                .filter(text -> text.equals("aa")
                        || text.equals("ab")
                        || text.equals("ac")
                        || text.equals("ba")
                        || text.equals("bb"))
                .toList();

        assertThat(texts).containsExactly("aa", "ab", "ac", "ba", "bb");
    }

    @Test
    @DisplayName("reader 생성 시 upperBound 포맷이 잘못되면 예외를 던진다")
    void shouldThrowWhenUpperBoundUpdatedAtIsInvalid() {
        assertThatThrownBy(() -> config.searchQueryUpsertReader("invalid-date")).isInstanceOf(RuntimeException.class);
    }

    private PagingQueryProvider queryProviderOf(JdbcPagingItemReader<?> reader) {
        return (PagingQueryProvider) ReflectionTestUtils.getField(reader, "queryProvider");
    }

    private <T> List<T> readAll(ItemStreamReader<T> reader) throws Exception {
        List<T> items = new ArrayList<>();
        reader.open(new ExecutionContext());
        try {
            T item;
            while ((item = reader.read()) != null) {
                items.add(item);
            }
        } finally {
            reader.close();
        }
        return items;
    }

    private <T> List<T> readFirstN(ItemStreamReader<T> reader, int expectedCount) throws Exception {
        List<T> items = new ArrayList<>();
        reader.open(new ExecutionContext());
        try {
            for (int i = 0; i < expectedCount; i++) {
                T item = reader.read();
                assertThat(item).isNotNull();
                items.add(item);
            }
        } finally {
            reader.close();
        }
        return items;
    }

    private void clearAllTables() {
        cursorJpaRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM recipe_tag");
        jdbcTemplate.update("DELETE FROM recipe_ingredient");
        jdbcTemplate.update("DELETE FROM recipe_detail_meta");
        jdbcTemplate.update("DELETE FROM recipe_youtube_meta");
        jdbcTemplate.update("DELETE FROM recipe");
    }

    private void registerMysqlCompatibility() {
        String className = SearchIndexingBatchConfigTest.class.getName();
        try {
            jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS BIN_TO_UUID FOR '" + className + ".binToUuid'");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS UUID_TO_BIN FOR '" + className + ".uuidToBin'");
        } catch (Exception ignored) {
        }
        try {
            jdbcTemplate.execute("CREATE DOMAIN IF NOT EXISTS UNSIGNED AS BIGINT");
        } catch (Exception ignored) {
        }
    }

    public static String binToUuid(UUID value) {
        return value.toString();
    }

    public static UUID uuidToBin(String value) {
        return UUID.fromString(value);
    }

    private void insertRecipe(UUID id, String recipeStatus, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                INSERT INTO recipe (
                    id, view_count, created_at, updated_at, recipe_status, source_type, source_key, credit_cost, current_job_id, is_public, market, country_code
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                0,
                Timestamp.valueOf(updatedAt.minusMinutes(10)),
                Timestamp.valueOf(updatedAt),
                recipeStatus,
                "YOUTUBE",
                id.toString(),
                1L,
                UUID.randomUUID(),
                false,
                "KR",
                "KR");
    }

    private void insertRecipeDetailMeta(UUID recipeId, int servings, LocalDateTime createdAt) {
        jdbcTemplate.update(
                """
                INSERT INTO recipe_detail_meta (
                    id, cook_time, servings, description, title, created_at, recipe_id, market, country_code
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                10,
                servings,
                "description",
                "title",
                Timestamp.valueOf(createdAt),
                recipeId,
                "KR",
                "KR");
    }

    private void insertRecipeYoutubeMeta(UUID recipeId, String title, String channelTitle, LocalDateTime at) {
        jdbcTemplate.update(
                """
                INSERT INTO recipe_youtube_meta (
                    id, video_id, title, channel_title, thumbnail_url, video_seconds, created_at, recipe_id, type, market, country_code
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                UUID.randomUUID().toString().replace("-", ""),
                title,
                channelTitle,
                ("https://img.youtube.com/" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8),
                30,
                Timestamp.valueOf(at),
                recipeId,
                "NORMAL",
                "KR",
                "KR");
    }

    private void insertRecipeIngredient(UUID recipeId, String name, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO recipe_ingredient (
                    id, name, unit, amount, recipe_id, created_at, market, country_code
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), name, "g", 1, recipeId, Timestamp.valueOf(createdAt), "KR", "KR");
    }

    private void insertRecipeTag(UUID recipeId, String tag, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO recipe_tag (
                    id, tag, created_at, recipe_id, market, country_code
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), tag, Timestamp.valueOf(createdAt), recipeId, "KR", "KR");
    }
}
