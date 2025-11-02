package com.cheftory.api.affiliate.coupang;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
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
    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

    // 테스트용 프로퍼티 (임의 키)
    properties = new CoupangPartnersProperties();
    properties.setAccessKey("test-access-key-1234");
    properties.setSecretKey("test-secret-key-1234");

    coupangClient = new CoupangClient(webClient, properties);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Nested
  @DisplayName("상품 검색")
  class SearchProducts {

    @Test
    @DisplayName("성공 응답을 정상 파싱한다")
    void shouldReturnSearchResponseSuccessfully() throws Exception {
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

      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody(json));

      // when
      CoupangSearchResponse res = coupangClient.searchProducts("Water");

      // then: 응답 본문 파싱 검증
      assertThat(res).isNotNull();
      assertThat(res.rCode()).isEqualTo("0");
      assertThat(res.data()).isNotNull();
      assertThat(res.data().productData()).hasSize(1);
      var p = res.data().productData().get(0);
      assertThat(p.productName()).isEqualTo("탐사 소프트 3겹 롤화장지");
      assertThat(p.productPrice()).isEqualTo(15600);
      assertThat(p.isFreeShipping()).isTrue();

      // then: 요청 형식 검증
      RecordedRequest req = mockWebServer.takeRequest();
      assertThat(req.getMethod()).isEqualTo("GET");
      assertThat(req.getPath())
          .isEqualTo("/v2/providers/affiliate_open_api/apis/openapi/products/search?keyword=Water");
      assertThat(req.getHeader("Authorization")).isNotBlank();
      assertThat(req.getHeader(HttpHeaders.ACCEPT)).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("한글 키워드가 올바르게 인코딩되어 전송된다")
    void shouldEncodeKoreanKeywordProperly() throws Exception {
      // given
      String okJson =
          """
          {"rCode":"0","rMessage":"","data":{"landingUrl":"https://...","productData":[]}}
          """;
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody(okJson));

      // when
      coupangClient.searchProducts("물티슈");

      // then
      RecordedRequest req = mockWebServer.takeRequest();
      assertThat(req.getMethod()).isEqualTo("GET");
      // %EB%AC%BC%ED%8B%B0%EC%8A%88 = "물티슈" UTF-8 인코딩
      assertThat(req.getPath())
          .isEqualTo(
              "/v2/providers/affiliate_open_api/apis/openapi/products/search?keyword=%EB%AC%BC%ED%8B%B0%EC%8A%88");
    }

    @Test
    @DisplayName("서버가 500을 반환하면 CoupangException을 던진다")
    void shouldThrowOn500() {
      // given
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(500)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody("{\"error\":\"Internal Server Error\"}"));

      // when & then
      assertThatThrownBy(() -> coupangClient.searchProducts("error-keyword"))
          .isInstanceOf(CoupangException.class)
          .hasFieldOrPropertyWithValue("errorMessage", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    }

    @Test
    @DisplayName("서버가 400을 반환하면 CoupangException을 던진다")
    void shouldThrowOn400() {
      // given
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(400)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody("{\"error\":\"Bad Request\"}"));

      // when & then
      assertThatThrownBy(() -> coupangClient.searchProducts("bad-request"))
          .isInstanceOf(CoupangException.class)
          .hasFieldOrPropertyWithValue("errorMessage", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    }

    @Test
    @DisplayName("잘못된 JSON이면 CoupangException을 던진다")
    void shouldThrowOnInvalidJson() {
      // given
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody("{ invalid json }"));

      // when & then
      assertThatThrownBy(() -> coupangClient.searchProducts("invalid-json"))
          .isInstanceOf(CoupangException.class)
          .hasFieldOrPropertyWithValue("errorMessage", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    }

    @Test
    @DisplayName("네트워크 실패 시 CoupangException을 던진다")
    void shouldThrowOnNetworkFailure() throws IOException {
      // given: 서버 종료로 연결 실패 시뮬레이션
      mockWebServer.shutdown();

      // when & then
      assertThatThrownBy(() -> coupangClient.searchProducts("any"))
          .isInstanceOf(CoupangException.class)
          .hasFieldOrPropertyWithValue("errorMessage", CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    }
  }
}
