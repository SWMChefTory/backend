package com.cheftory.api.search.indexing.support;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenSearchBulkClient implements SearchIndexingBulkClient {

    private final OpenSearchClient openSearchClient;

    @Override
    public void bulkIndex(String indexName, List<BulkIndexPayload> payloads) throws SearchException {
        if (payloads.isEmpty()) {
            return;
        }

        try {
            BulkResponse response = openSearchClient.bulk(builder -> {
                builder.index(indexName);
                payloads.forEach(payload -> builder.operations(
                        op -> op.index(idx -> idx.id(payload.id()).document(payload.document()))));
                return builder;
            });

            validateBulkResponse("index", indexName, response);
        } catch (SearchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Bulk index failed. index={}", indexName, e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, e);
        }
    }

    @Override
    public void bulkDelete(String indexName, List<String> ids) throws SearchException {
        if (ids.isEmpty()) {
            return;
        }

        try {
            BulkResponse response = openSearchClient.bulk(builder -> {
                builder.index(indexName);
                ids.forEach(id -> builder.operations(op -> op.delete(del -> del.id(id))));
                return builder;
            });

            validateBulkResponse("delete", indexName, response);
        } catch (SearchException e) {
            throw e;
        } catch (Exception e) {
            log.error("Bulk delete failed. index={}", indexName, e);
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, e);
        }
    }

    private void validateBulkResponse(String operation, String indexName, BulkResponse response)
            throws SearchException {
        if (!response.errors()) {
            return;
        }

        String failures = response.items().stream()
                .filter(item -> item.error() != null)
                .limit(5)
                .map(item -> String.format(
                        "id=%s,status=%d,error=%s",
                        item.id(), item.status(), item.error().reason()))
                .collect(Collectors.joining("; "));

        log.error(
                "Bulk {} has failures. index={}, took={}, failures={}",
                operation,
                indexName,
                response.took(),
                failures);
        throw new SearchException(SearchErrorCode.SEARCH_FAILED);
    }
}
