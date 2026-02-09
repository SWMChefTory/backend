package com.cheftory.api.affiliate.coupang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;
import java.io.IOException;
import java.util.List;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Headers;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("CoupangClient 테스트")
class CoupangClientTest {

    private MockWebServer mockWebServer;
    private CoupangClient coupangClient;
    private CoupangPartnersProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // WebClient는 mock 서버를 baseUrl로 사용
        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        // 테스트용 프로퍼티 (임의 키)
        properties = new CoupangPartnersProperties();
        properties.setAccessKey("test-access-key-1234");
        properties.setSecretKey("test-secret-key-1234");

        coupangClient = new CoupangClient(webClient, properties);
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("상품 검색")
    class SearchProducts {

        @Test
        @DisplayName("성공 응답을 정상 파싱하고 CoupangProducts로 변환한다")
        void shouldReturnProductListSuccessfully() throws Exception {
            // given
            String json =
                    """
          {
            "rCode": "0",
            "rMessage": "",
            "data": {
              "landingUrl": "https://link.coupang.com/re/AFFSRP?...",
              "productData": [
                {
                  "keyword": "Water",
                  "rank": 12,
                  "isRocket": false,
                  "isFreeShipping": true,
                  "productId": 27664441,
                  "productImage": "https://ads-partners.coupang.com/image1/abc.png",
                  "productName": "탐사 소프트 3겹 롤화장지",
                  "productPrice": 15600,
                  "productUrl": "https://link.coupang.com/re/AFFSDP?..."
                }
              ]
            }
          }
          """;

            mockWebServer.enqueue(new MockResponse(
                    200, Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE), json));

            // when
            CoupangProducts result = coupangClient.searchProducts("Water");
            List<CoupangProduct> products = result.getCoupangProducts();

            // then: CoupangProduct 리스트 검증
            assertThat(products).isNotNull();
            assertThat(products).hasSize(1);

            CoupangProduct product = products.getFirst();
            assertThat(product.getKeyword()).isEqualTo("Water");
            assertThat(product.getRank()).isEqualTo(12);
            assertThat(product.getIsRocket()).isFalse();
            assertThat(product.getIsFreeShipping()).isTrue();
            assertThat(product.getProductId()).isEqualTo(27664441L);
            assertThat(product.getProductImage()).isEqualTo("https://ads-partners.coupang.com/image1/abc.png");
            assertThat(product.getProductName()).isEqualTo("탐사 소프트 3겹 롤화장지");
            assertThat(product.getProductPrice()).isEqualTo(15600);
            assertThat(product.getProductUrl()).isEqualTo("https://link.coupang.com/re/AFFSDP?...");

            // then: 요청 형식 검증
            RecordedRequest req = mockWebServer.takeRequest();
            assertThat(req.getMethod()).isEqualTo("GET");
            assertThat(req.getTarget())
                    .isEqualTo("/v2/providers/affiliate_open_api/apis/openapi/products/search?keyword=Water");
            assertThat(req.getHeaders().get("Authorization")).isNotBlank();
            assertThat(req.getHeaders().get(HttpHeaders.ACCEPT)).contains(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        @DisplayName("여러 상품이 포함된 응답을 올바르게 변환한다")
        void shouldReturnMultipleProducts() throws Exception {
            // given
            String json =
                    """
          {
            "rCode": "0",
            "rMessage": "",
            "data": {
              "landingUrl": "https://link.coupang.com/re/AFFSRP?...",
              "productData": [
                {
                  "keyword": "노트북",
                  "rank": 1,
                  "isRocket": true,
                  "isFreeShipping": true,
                  "productId": 1001,
                  "productImage": "https://image1.jpg",
                  "productName": "노트북1",
                  "productPrice": 1000000,
                  "productUrl": "https://url1"
                },
                {
                  "keyword": "노트북",
                  "rank": 2,
                  "isRocket": false,
                  "isFreeShipping": true,
                  "productId": 1002,
                  "productImage": "https://image2.jpg",
                  "productName": "노트북2",
                  "productPrice": 1500000,
                  "productUrl": "https://url2"
                }
              ]
            }
          }
          """;

            mockWebServer.enqueue(new MockResponse(
                    200, Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE), json));

            // when
            CoupangProducts result = coupangClient.searchProducts("노트북");
            List<CoupangProduct> products = result.getCoupangProducts();

            // then
            assertThat(products).hasSize(2);
            assertThat(products.get(0).getProductName()).isEqualTo("노트북1");
            assertThat(products.get(0).getIsRocket()).isTrue();
            assertThat(products.get(1).getProductName()).isEqualTo("노트북2");
            assertThat(products.get(1).getIsRocket()).isFalse();
        }

        @Test
        @DisplayName("빈 상품 목록을 반환하면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            // given
            String json =
                    """
          {
            "rCode": "0",
            "rMessage": "",
            "data": {
              "landingUrl": "https://link.coupang.com/re/AFFSRP?...",
              "productData": []
            }
          }
          """;

            mockWebServer.enqueue(new MockResponse(
                    200, Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE), json));

            // when
            CoupangProducts result = coupangClient.searchProducts("없는상품");
            List<CoupangProduct> products = result.getCoupangProducts();

            // then
            assertThat(products).isEmpty();
        }

        @Test
        @DisplayName("서버가 500을 반환하면 CoupangException을 던진다")
        void shouldThrowOn500() {
            // given

            String responseBody = "{\"error\":\"Internal Server Error\"}";
            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(500)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseBody)
                    .build());
            // when & then
            assertThatThrownBy(() -> coupangClient.searchProducts("error-keyword"))
                    .isInstanceOf(CoupangException.class)
                    .hasFieldOrPropertyWithValue("error", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
        }

        @Test
        @DisplayName("서버가 400을 반환하면 CoupangException을 던진다")
        void shouldThrowOn400() {
            // given
            mockWebServer.enqueue(new MockResponse(
                    400,
                    Headers.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE),
                    "{\"error\":\"Bad Request\"}"));

            // when & then
            assertThatThrownBy(() -> coupangClient.searchProducts("bad-request"))
                    .isInstanceOf(CoupangException.class)
                    .hasFieldOrPropertyWithValue("error", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
        }

        @Test
        @DisplayName("잘못된 JSON이면 CoupangException을 던진다")
        void shouldThrowOnInvalidJson() {
            // given
            String responseBody = "{ invalid json }";
            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(200)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(responseBody)
                    .build());

            // when & then
            assertThatThrownBy(() -> coupangClient.searchProducts("invalid-json"))
                    .isInstanceOf(CoupangException.class)
                    .hasFieldOrPropertyWithValue("error", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
        }

        @Test
        @DisplayName("네트워크 실패 시 CoupangException을 던진다")
        void shouldThrowOnNetworkFailure() {
            mockWebServer.close();

            // when & then
            assertThatThrownBy(() -> coupangClient.searchProducts("any"))
                    .isInstanceOf(CoupangException.class)
                    .hasFieldOrPropertyWithValue("error", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
        }
    }
}
