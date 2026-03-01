package com.cheftory.api.search.indexing.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenSearchTemplateInitializer 테스트")
class OpenSearchTemplateInitializerTest {

    @Mock
    private SearchIndexingTemplateClient templateClient;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private OpenSearchTemplateInitializer initializer;

    @Test
    @DisplayName("실행 시 템플릿 적용 후 인덱스를 보장한다")
    void shouldApplyTemplatesAndEnsureIndices() throws Exception {

        ReflectionTestUtils.setField(initializer, "enabled", true);

        when(resourceLoader.getResource("classpath:opensearch/templates/autocomplete-template.json"))
                .thenReturn(asResource("{\"name\":\"autocomplete\"}"));
        when(resourceLoader.getResource("classpath:opensearch/templates/search-query-template.json"))
                .thenReturn(asResource("{\"name\":\"search_query\"}"));

        initializer.run(new DefaultApplicationArguments(new String[] {}));

        InOrder inOrder = inOrder(templateClient);
        inOrder.verify(templateClient).putIndexTemplate("autocomplete", "{\"name\":\"autocomplete\"}");
        inOrder.verify(templateClient).putIndexTemplate("search_query", "{\"name\":\"search_query\"}");
        inOrder.verify(templateClient).ensureIndexExists("autocomplete");
        inOrder.verify(templateClient).ensureIndexExists("search_query");
    }

    @Test
    @DisplayName("템플릿 리소스 읽기에 실패하면 SEARCH_FAILED 예외를 던진다")
    void shouldThrowWhenResourceReadFails() {

        ReflectionTestUtils.setField(initializer, "enabled", true);

        Resource broken = org.mockito.Mockito.mock(Resource.class);
        try {
            when(broken.getInputStream()).thenThrow(new IOException("read failed"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(resourceLoader.getResource("classpath:opensearch/templates/autocomplete-template.json"))
                .thenReturn(broken);

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments(new String[] {})))
                .isInstanceOf(SearchException.class)
                .hasFieldOrPropertyWithValue("error", SearchErrorCode.SEARCH_FAILED);
    }

    private ByteArrayResource asResource(String json) {
        return new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
    }
}
