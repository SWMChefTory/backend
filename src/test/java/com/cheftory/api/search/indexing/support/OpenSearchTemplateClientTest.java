package com.cheftory.api.search.indexing.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.transport.endpoints.BooleanResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenSearchTemplateClient 테스트")
class OpenSearchTemplateClientTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private OpenSearchGenericClient genericClient;

    @Mock
    private Response genericResponse;

    @Mock
    private Body responseBody;

    @Mock
    private OpenSearchIndicesClient indicesClient;

    @InjectMocks
    private OpenSearchTemplateClient templateClient;

    @Test
    @DisplayName("putIndexTemplate은 2xx 응답이면 정상 처리한다")
    void shouldPutIndexTemplateSuccessfully() throws Exception {
        when(openSearchClient.generic()).thenReturn(genericClient);
        when(genericClient.execute(any(Request.class))).thenReturn(genericResponse);
        when(genericResponse.getStatus()).thenReturn(200);

        templateClient.putIndexTemplate("autocomplete", "{\"index_patterns\":[\"autocomplete*\"]}");

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(genericClient).execute(captor.capture());
        Request request = captor.getValue();
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getEndpoint()).isEqualTo("/_index_template/autocomplete");
        assertThat(request.getBody()).isPresent();
    }

    @Test
    @DisplayName("putIndexTemplate은 3xx 이상 응답이면 SEARCH_FAILED 예외를 던진다")
    void shouldThrowWhenPutIndexTemplateStatusIsError() throws Exception {
        when(openSearchClient.generic()).thenReturn(genericClient);
        when(genericClient.execute(any(Request.class))).thenReturn(genericResponse);
        when(genericResponse.getStatus()).thenReturn(400);
        when(genericResponse.getReason()).thenReturn("Bad Request");
        when(genericResponse.getBody()).thenReturn(Optional.of(responseBody));
        when(responseBody.bodyAsString()).thenReturn("{\"error\":\"invalid\"}");

        assertThatThrownBy(() -> templateClient.putIndexTemplate("search_query", "{\"x\":1}"))
                .isInstanceOf(SearchException.class)
                .hasFieldOrPropertyWithValue("error", SearchErrorCode.SEARCH_FAILED);
    }

    @Test
    @DisplayName("putIndexTemplate은 execute IOException을 SEARCH_FAILED 예외로 변환한다")
    void shouldWrapIOExceptionWhenPutIndexTemplate() throws Exception {
        when(openSearchClient.generic()).thenReturn(genericClient);
        when(genericClient.execute(any(Request.class))).thenThrow(new IOException("io error"));

        assertThatThrownBy(() -> templateClient.putIndexTemplate("search_query", "{\"x\":1}"))
                .isInstanceOf(SearchException.class)
                .hasFieldOrPropertyWithValue("error", SearchErrorCode.SEARCH_FAILED)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("ensureIndexExists는 인덱스가 이미 있으면 create를 호출하지 않는다")
    void shouldSkipCreateWhenIndexExists() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(java.util.function.Function.class))).thenReturn(new BooleanResponse(true));

        templateClient.ensureIndexExists("autocomplete");

        verify(indicesClient, never()).create(any(java.util.function.Function.class));
    }

    @Test
    @DisplayName("ensureIndexExists는 인덱스가 없으면 create를 호출한다")
    void shouldCreateWhenIndexNotExists() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(java.util.function.Function.class))).thenReturn(new BooleanResponse(false));
        when(indicesClient.create(any(java.util.function.Function.class)))
                .thenReturn(CreateIndexResponse.of(
                        b -> b.acknowledged(true).shardsAcknowledged(true).index("autocomplete")));

        templateClient.ensureIndexExists("autocomplete");

        verify(indicesClient).create(any(java.util.function.Function.class));
    }

    @Test
    @DisplayName("ensureIndexExists는 OpenSearch 예외를 SEARCH_FAILED로 변환한다")
    void shouldWrapExceptionWhenEnsureIndexExistsFails() throws Exception {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(java.util.function.Function.class))).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> templateClient.ensureIndexExists("autocomplete"))
                .isInstanceOf(SearchException.class)
                .hasFieldOrPropertyWithValue("error", SearchErrorCode.SEARCH_FAILED);
    }
}
