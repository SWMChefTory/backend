package com.cheftory.api.search.indexing.config;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class SearchIndexingBatchConfig {

    private static final String INDEX_AUTOCOMPLETE = "autocomplete";
    private static final String INDEX_SEARCH_QUERY = "search_query";

    private static final String PIPELINE_SEARCH_QUERY_UPSERT = "search-query-upsert";
    private static final String PIPELINE_SEARCH_QUERY_DELETE = "search-query-delete";

    private final DataSource dataSource;
    private final SearchIndexingBulkClient bulkClient;
    private final IndexingCursorRepository cursorRepository;
    private final ObjectMapper objectMapper;

    @Value("${search.indexing.batch-size}")
    private int batchSize;

    @Value("${search.indexing.timezone}")
    private String timezone;

    @Bean
    public Job autocompleteIndexJob(JobRepository jobRepository, Step autocompleteIndexStep) {
        return new JobBuilder("autocompleteIndexJob", jobRepository)
                .start(autocompleteIndexStep)
                .build();
    }

    @Bean
    public Job searchQueryUpsertIndexJob(JobRepository jobRepository, Step searchQueryUpsertIndexStep) {
        return new JobBuilder("searchQueryUpsertIndexJob", jobRepository)
                .start(searchQueryUpsertIndexStep)
                .build();
    }

    @Bean
    public Job searchQueryDeleteIndexJob(JobRepository jobRepository, Step searchQueryDeleteIndexStep) {
        return new JobBuilder("searchQueryDeleteIndexJob", jobRepository)
                .start(searchQueryDeleteIndexStep)
                .build();
    }

    @Bean
    public Step autocompleteIndexStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<AutocompleteAggregateRow> autocompleteReader,
            ItemWriter<AutocompleteAggregateRow> autocompleteWriter) {
        return new StepBuilder("autocompleteIndexStep", jobRepository)
                .<AutocompleteAggregateRow, AutocompleteAggregateRow>chunk(batchSize)
                .transactionManager(transactionManager)
                .reader(autocompleteReader)
                .writer(autocompleteWriter)
                .build();
    }

    @Bean
    public Step searchQueryUpsertIndexStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<SearchQueryUpsertRow> searchQueryUpsertReader,
            ItemWriter<SearchQueryUpsertRow> searchQueryUpsertWriter) {
        return new StepBuilder("searchQueryUpsertIndexStep", jobRepository)
                .<SearchQueryUpsertRow, SearchQueryUpsertRow>chunk(batchSize)
                .transactionManager(transactionManager)
                .reader(searchQueryUpsertReader)
                .writer(searchQueryUpsertWriter)
                .build();
    }

    @Bean
    public Step searchQueryDeleteIndexStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<SearchQueryDeleteRow> searchQueryDeleteReader,
            ItemWriter<SearchQueryDeleteRow> searchQueryDeleteWriter) {
        return new StepBuilder("searchQueryDeleteIndexStep", jobRepository)
                .<SearchQueryDeleteRow, SearchQueryDeleteRow>chunk(batchSize)
                .transactionManager(transactionManager)
                .reader(searchQueryDeleteReader)
                .writer(searchQueryDeleteWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<SearchQueryUpsertRow> searchQueryUpsertReader(
            @Value("#{jobParameters['upperBoundUpdatedAt']}") String upperBoundUpdatedAtParam) throws Exception {
        UpdatedAtIdCursor cursor = cursorRepository.load(PIPELINE_SEARCH_QUERY_UPSERT);
        LocalDateTime upperBoundUpdatedAt = LocalDateTime.parse(upperBoundUpdatedAtParam);

        Map<String, Object> parameterValues = new LinkedHashMap<>();
        parameterValues.put("last_updated_at", Timestamp.valueOf(cursor.lastUpdatedAt()));
        parameterValues.put("last_id", cursor.lastId().toString());
        parameterValues.put("upper_bound_updated_at", Timestamp.valueOf(upperBoundUpdatedAt));

        return new JdbcPagingItemReaderBuilder<SearchQueryUpsertRow>()
                .name("searchQueryUpsertReader")
                .dataSource(dataSource)
                .queryProvider(searchQueryUpsertQueryProvider())
                .parameterValues(parameterValues)
                .rowMapper(searchQueryUpsertRowMapper())
                .pageSize(batchSize)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<SearchQueryDeleteRow> searchQueryDeleteReader(
            @Value("#{jobParameters['upperBoundUpdatedAt']}") String upperBoundUpdatedAtParam) throws Exception {
        UpdatedAtIdCursor cursor = cursorRepository.load(PIPELINE_SEARCH_QUERY_DELETE);
        LocalDateTime upperBoundUpdatedAt = LocalDateTime.parse(upperBoundUpdatedAtParam);

        Map<String, Object> parameterValues = new LinkedHashMap<>();
        parameterValues.put("last_updated_at", Timestamp.valueOf(cursor.lastUpdatedAt()));
        parameterValues.put("last_id", cursor.lastId().toString());
        parameterValues.put("upper_bound_updated_at", Timestamp.valueOf(upperBoundUpdatedAt));

        return new JdbcPagingItemReaderBuilder<SearchQueryDeleteRow>()
                .name("searchQueryDeleteReader")
                .dataSource(dataSource)
                .queryProvider(searchQueryDeleteQueryProvider())
                .parameterValues(parameterValues)
                .rowMapper((rs, rowNum) -> new SearchQueryDeleteRow(
                        rs.getString("id"),
                        requireTimestamp(rs.getTimestamp("updated_at"), "updated_at")
                                .toLocalDateTime()))
                .pageSize(batchSize)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<AutocompleteAggregateRow> autocompleteReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<AutocompleteAggregateRow>()
                .name("autocompleteReader")
                .dataSource(dataSource)
                .queryProvider(autocompleteQueryProvider())
                .rowMapper((rs, rowNum) -> new AutocompleteAggregateRow(
                        rs.getString("market"), rs.getString("text"), rs.getString("scope"), rs.getInt("count")))
                .pageSize(batchSize)
                .build();
    }

    @Bean
    public ItemWriter<SearchQueryUpsertRow> searchQueryUpsertWriter() {
        return chunk -> {
            List<? extends SearchQueryUpsertRow> rows = chunk.getItems();
            if (rows.isEmpty()) {
                return;
            }

            List<BulkIndexPayload> payloads =
                    rows.stream().map(this::toSearchQueryPayload).toList();
            bulkClient.bulkIndex(INDEX_SEARCH_QUERY, payloads);

            SearchQueryUpsertRow last = rows.getLast();
            cursorRepository.save(
                    PIPELINE_SEARCH_QUERY_UPSERT,
                    new UpdatedAtIdCursor(last.updatedAt(), toUuid(last.id(), PIPELINE_SEARCH_QUERY_UPSERT)));
        };
    }

    @Bean
    public ItemWriter<SearchQueryDeleteRow> searchQueryDeleteWriter() {
        return chunk -> {
            List<? extends SearchQueryDeleteRow> rows = chunk.getItems();
            if (rows.isEmpty()) {
                return;
            }

            List<String> ids = rows.stream().map(SearchQueryDeleteRow::id).toList();
            bulkClient.bulkDelete(INDEX_SEARCH_QUERY, ids);

            SearchQueryDeleteRow last = rows.getLast();
            cursorRepository.save(
                    PIPELINE_SEARCH_QUERY_DELETE,
                    new UpdatedAtIdCursor(last.updatedAt(), toUuid(last.id(), PIPELINE_SEARCH_QUERY_DELETE)));
        };
    }

    @Bean
    public ItemWriter<AutocompleteAggregateRow> autocompleteWriter() {
        return chunk -> {
            List<? extends AutocompleteAggregateRow> rows = chunk.getItems();
            if (rows.isEmpty()) {
                return;
            }

            List<BulkIndexPayload> payloads =
                    rows.stream().map(this::toAutocompletePayload).toList();
            bulkClient.bulkIndex(INDEX_AUTOCOMPLETE, payloads);
        };
    }

    private MySqlPagingQueryProvider searchQueryUpsertQueryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("""
                SELECT
                    BIN_TO_UUID(r.id) AS id,
                    LOWER(r.market) AS market,
                    yi.title,
                    yi.channel_title,
                    'recipe' AS scope,
                    CONCAT(rdm.servings, '인분') AS servings_text,
                    (
                        SELECT COALESCE(JSON_ARRAYAGG(ri.name), JSON_ARRAY())
                        FROM recipe_ingredient ri
                        WHERE ri.recipe_id = r.id
                    ) AS ingredients_json,
                    (
                        SELECT COALESCE(JSON_ARRAYAGG(rt.tag), JSON_ARRAY())
                        FROM recipe_tag rt
                        WHERE rt.recipe_id = r.id
                    ) AS tags_json,
                    r.created_at,
                    r.updated_at
                """);
        queryProvider.setFromClause("""
                FROM recipe r
                LEFT JOIN recipe_youtube_meta yi ON r.id = yi.recipe_id
                LEFT JOIN recipe_detail_meta rdm ON r.id = rdm.recipe_id
                """);
        queryProvider.setWhereClause("""
                WHERE
                    r.recipe_status = 'SUCCESS'
                    AND r.updated_at <= :upper_bound_updated_at
                    AND (
                        r.updated_at > :last_updated_at
                        OR (
                            r.updated_at = :last_updated_at
                            AND r.id > UUID_TO_BIN(:last_id)
                        )
                    )
                """);

        LinkedHashMap<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("r.updated_at", Order.ASCENDING);
        sortKeys.put("r.id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        return queryProvider;
    }

    private MySqlPagingQueryProvider searchQueryDeleteQueryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("""
                SELECT
                    BIN_TO_UUID(r.id) AS id,
                    r.updated_at
                """);
        queryProvider.setFromClause("FROM recipe r");
        queryProvider.setWhereClause("""
                WHERE
                    r.recipe_status IN ('BLOCKED', 'FAILED')
                    AND r.updated_at <= :upper_bound_updated_at
                    AND (
                        r.updated_at > :last_updated_at
                        OR (
                            r.updated_at = :last_updated_at
                            AND r.id > UUID_TO_BIN(:last_id)
                        )
                    )
                """);

        LinkedHashMap<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("r.updated_at", Order.ASCENDING);
        sortKeys.put("r.id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        return queryProvider;
    }

    private MySqlPagingQueryProvider autocompleteQueryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("""
                SELECT
                    all_text.market,
                    all_text.text,
                    'recipe' AS scope,
                    CAST(ROUND(SUM(all_text.weight)) AS UNSIGNED) AS count
                """);
        queryProvider.setFromClause("""
                FROM (
                    SELECT
                        LOWER(r.market) AS market,
                        yi.title AS text,
                        1.0 AS weight
                    FROM recipe r
                    LEFT JOIN recipe_youtube_meta yi ON r.id = yi.recipe_id
                    WHERE r.recipe_status = 'SUCCESS' AND yi.title IS NOT NULL

                    UNION ALL

                    SELECT
                        LOWER(r.market) AS market,
                        yi.channel_title AS text,
                        0.5 AS weight
                    FROM recipe r
                    LEFT JOIN recipe_youtube_meta yi ON r.id = yi.recipe_id
                    WHERE r.recipe_status = 'SUCCESS' AND yi.channel_title IS NOT NULL

                    UNION ALL

                    SELECT
                        LOWER(r.market) AS market,
                        i.name AS text,
                        1.0 AS weight
                    FROM recipe_ingredient i
                    JOIN recipe r ON i.recipe_id = r.id
                    WHERE r.recipe_status = 'SUCCESS'

                    UNION ALL

                    SELECT
                        LOWER(r.market) AS market,
                        t.tag AS text,
                        1.0 AS weight
                    FROM recipe_tag t
                    JOIN recipe r ON t.recipe_id = r.id
                    WHERE r.recipe_status = 'SUCCESS'

                    UNION ALL

                    SELECT
                        LOWER(r.market) AS market,
                        CONCAT(rdm.servings, '인분') AS text,
                        0.1 AS weight
                    FROM recipe_detail_meta rdm
                    JOIN recipe r ON rdm.recipe_id = r.id
                    WHERE r.recipe_status = 'SUCCESS'
                      AND rdm.servings IS NOT NULL
                ) AS all_text
                """);
        queryProvider.setGroupClause("GROUP BY all_text.market, all_text.text");

        LinkedHashMap<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("all_text.market", Order.ASCENDING);
        sortKeys.put("all_text.text", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        return queryProvider;
    }

    private RowMapper<SearchQueryUpsertRow> searchQueryUpsertRowMapper() {
        return (rs, rowNum) -> new SearchQueryUpsertRow(
                requiredNonBlank(rs.getString("id"), "id"),
                rs.getString("market"),
                rs.getString("title"),
                rs.getString("channel_title"),
                rs.getString("scope"),
                rs.getString("servings_text"),
                parseJsonArray(rs.getString("ingredients_json")),
                parseJsonArray(rs.getString("tags_json")),
                requireTimestamp(rs.getTimestamp("created_at"), "created_at").toLocalDateTime(),
                requireTimestamp(rs.getTimestamp("updated_at"), "updated_at").toLocalDateTime());
    }

    private List<String> parseJsonArray(String jsonArrayString) {
        if (jsonArrayString == null || jsonArrayString.isBlank()) {
            return List.of();
        }

        try {
            JsonNode node = objectMapper.readTree(jsonArrayString);
            if (!node.isArray()) {
                throw new IllegalStateException("Expected JSON array but got: " + jsonArrayString);
            }
            return StreamSupport.stream(node.spliterator(), false)
                    .map(JsonNode::asText)
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JSON array: " + jsonArrayString, e);
        }
    }

    private BulkIndexPayload toSearchQueryPayload(SearchQueryUpsertRow row) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", row.id());
        document.put("market", row.market());
        document.put("scope", row.scope());
        document.put("title", row.title());
        document.put("channel_title", row.channelTitle());
        document.put("servings_text", row.servingsText());
        document.put("ingredients", row.ingredients());
        document.put("keywords", row.keywords());
        document.put("created_at", toEpochMillis(row.createdAt()));
        document.put("updated_at", toEpochMillis(row.updatedAt()));

        return new BulkIndexPayload(row.id(), document);
    }

    private BulkIndexPayload toAutocompletePayload(AutocompleteAggregateRow row) {
        String market = requiredNonBlank(row.market(), "market").toLowerCase(Locale.ROOT);
        String scope = requiredNonBlank(row.scope(), "scope").toLowerCase(Locale.ROOT);
        String text = requiredNonBlank(row.text(), "text");

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("market", market);
        document.put("scope", scope);
        document.put("text", text);
        document.put("count", row.count());

        return new BulkIndexPayload(market + ":" + scope + ":" + text, document);
    }

    private long toEpochMillis(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.of(timezone);
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli();
    }

    private String requiredNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required field is blank: " + fieldName);
        }
        return value.trim();
    }

    private Timestamp requireTimestamp(Timestamp timestamp, String fieldName) {
        if (timestamp == null) {
            throw new IllegalStateException("Required timestamp is null: " + fieldName);
        }
        return timestamp;
    }

    private UUID toUuid(String value, String pipelineName) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid UUID from pipeline " + pipelineName + ": " + value, e);
        }
    }
}
