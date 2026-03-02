package com.cheftory.api.search.indexing.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cheftory.api.search.exception.SearchErrorCode;
import com.cheftory.api.search.exception.SearchException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.OperationType;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenSearchBulkClient 테스트")
class OpenSearchBulkClientTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @InjectMocks
    private OpenSearchBulkClient bulkClient;

    @Test
    @DisplayName("bulkIndex는 payload가 비어 있으면 OpenSearch를 호출하지 않는다")
    void shouldSkipBulkIndexWhenPayloadsEmpty() throws SearchException {
        bulkClient.bulkIndex("search_query", List.of());

        verifyNoInteractions(openSearchClient);
    }

    @Test
    @DisplayName("bulkIndex는 OpenSearch 응답에 에러가 없으면 정상 종료한다")
    void shouldBulkIndexSuccessfully() throws Exception {
        BulkResponse response =
                BulkResponse.of(b -> b.errors(false).took(1L).items(i -> i.operationType(OperationType.Index)
                        .index("search_query")
                        .id("id-1")
                        .status(200)));
        when(openSearchClient.bulk(any(Function.class))).thenReturn(response);

        bulkClient.bulkIndex("search_query", List.of(new BulkIndexPayload("id-1", Map.of("title", "kimchi"))));

        verify(openSearchClient).bulk(any(Function.class));
    }

    @Test
    @DisplayName("bulkIndex는 부분 실패 응답이면 SEARCH_FAILED 예외를 던진다")
    void shouldThrowSearchExceptionWhenBulkIndexHasFailures() throws Exception {
        BulkResponse response =
                BulkResponse.of(b -> b.errors(true).took(2L).items(i -> i.operationType(OperationType.Index)
                        .index("search_query")
                        .id("id-1")
                        .status(400)
                        .error(e -> e.type("mapper_parsing_exception").reason("failed to parse field"))));
        when(openSearchClient.bulk(any(java.util.function.Function.class))).thenReturn(response);

        assertThatThrownBy(() -> bulkClient.bulkIndex("search_query", List.of(new BulkIndexPayload("id-1", Map.of()))))
                .isInstanceOf(SearchException.class)
                .hasFieldOrPropertyWithValue("error", SearchErrorCode.SEARCH_FAILED);
    }

    @Test
    @DisplayName("bulkDelete는 id가 비어 있으면 OpenSearch를 호출하지 않는다")
    void shouldSkipBulkDeleteWhenIdsEmpty() throws SearchException {
        bulkClient.bulkDelete("search_query", List.of());

        verifyNoInteractions(openSearchClient);
    }

    @Test
    @DisplayName("bulkDelete는 OpenSearch 예외를 SEARCH_FAILED 예외로 변환한다")
    void shouldWrapBulkDeleteException() throws Exception {
        when(openSearchClient.bulk(any(java.util.function.Function.class))).thenThrow(new IOException("io error"));

        assertThatThrownBy(() -> bulkClient.bulkDelete("search_query", List.of("id-1")))
                .isInstanceOf(SearchException.class)
                .hasFieldOrPropertyWithValue("error", SearchErrorCode.SEARCH_FAILED)
                .hasCauseInstanceOf(IOException.class);
    }
}
