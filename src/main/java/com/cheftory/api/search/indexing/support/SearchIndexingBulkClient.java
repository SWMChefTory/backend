package com.cheftory.api.search.indexing.support;

import com.cheftory.api.search.exception.SearchException;
import java.util.List;

public interface SearchIndexingBulkClient {

    void bulkIndex(String indexName, List<BulkIndexPayload> payloads) throws SearchException;

    void bulkDelete(String indexName, List<String> ids) throws SearchException;
}
