package com.cheftory.api.search.indexing.support;

import com.cheftory.api.search.exception.SearchException;

public interface SearchIndexingTemplateClient {

    void putIndexTemplate(String templateName, String templateJson) throws SearchException;

    void ensureIndexExists(String indexName) throws SearchException;
}
