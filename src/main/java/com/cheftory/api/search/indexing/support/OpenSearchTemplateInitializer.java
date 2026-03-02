package com.cheftory.api.search.indexing.support;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@RequiredArgsConstructor
public class OpenSearchTemplateInitializer implements ApplicationRunner {

    private final SearchIndexingTemplateClient templateClient;
    private final ResourceLoader resourceLoader;

    @Value("${search.indexing.enabled}")
    boolean enabled;

    @Override
    public void run(ApplicationArguments args) throws SearchException {

        if (!enabled) return;

        putIndexTemplate("autocomplete", "classpath:opensearch/templates/autocomplete-template.json");
        putIndexTemplate("search_query", "classpath:opensearch/templates/search-query-template.json");

        templateClient.ensureIndexExists("autocomplete");
        templateClient.ensureIndexExists("search_query");
    }

    private void putIndexTemplate(String templateName, String resourceLocation) throws SearchException {
        String templateJson = readResource(resourceLocation);
        templateClient.putIndexTemplate(templateName, templateJson);
    }

    private String readResource(String resourceLocation) throws SearchException {
        Resource resource = resourceLoader.getResource(resourceLocation);
        try (var inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SearchException(SearchErrorCode.SEARCH_FAILED, e);
        }
    }
}
