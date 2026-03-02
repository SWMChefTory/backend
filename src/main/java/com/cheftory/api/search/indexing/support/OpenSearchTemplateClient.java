package com.cheftory.api.search.indexing.support;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenSearchTemplateClient implements SearchIndexingTemplateClient {

    private final OpenSearchClient openSearchClient;

    @Override
    public void putIndexTemplate(String templateName, String templateJson) throws SearchException {
        Request request = Requests.builder()
                .method("PUT")
                .endpoint("/_index_template/" + templateName)
                .json(templateJson)
                .build();

        try (var response = openSearchClient.generic().execute(request)) {
            if (response.getStatus() >= 300) {
                String responseBody = response.getBody()
                        .map(body -> {
                            try {
                                return body.bodyAsString();
                            } catch (Exception e) {
                                return "";
                            }
                        })
                        .orElse("");

                log.error(
                        "Failed to put index template. name={}, status={}, reason={}, body={}",
                        templateName,
                        response.getStatus(),
                        response.getReason(),
                        responseBody);
                throw new SearchException(SearchErrorCode.SEARCH_FAILED);
            }

            log.info("Index template applied. name={}", templateName);
        } catch (IOException e) {
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, e);
        } catch (Exception e) {
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, e);
        }
    }

    @Override
    public void ensureIndexExists(String indexName) throws SearchException {
        try {
            boolean exists =
                    openSearchClient.indices().exists(e -> e.index(indexName)).value();
            if (exists) {
                return;
            }

            openSearchClient.indices().create(c -> c.index(indexName));
            log.info("Index created from template. index={}", indexName);
        } catch (Exception e) {
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, e);
        }
    }
}
