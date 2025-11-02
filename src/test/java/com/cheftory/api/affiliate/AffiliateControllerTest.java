package com.cheftory.api.affiliate;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import com.cheftory.api.affiliate.dto.AffiliateSearchResponse;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

@DisplayName("Affiliate Controller")
class AffiliateControllerTest extends RestDocsTest {

  private AffiliateService affiliateService;
  private AffiliateController controller;
  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    affiliateService = mock(AffiliateService.class);
    controller = new AffiliateController(affiliateService);
    exceptionHandler = new GlobalExceptionHandler();

    mockMvc = mockMvcBuilder(controller).withAdvice(exceptionHandler).build();
  }

  @Nested
  @DisplayName("쿠팡 상품 검색")
  class SearchCoupangProducts {

    @Nested
    @DisplayName("Given - 유효한 keyword가 주어졌을 때")
    class GivenValidKeyword {

      @Nested
      @DisplayName("When - 상품 검색을 요청하면")
      class WhenSearchingProducts {

        @Test
        @DisplayName("Then - 200과 상품 리스트를 반환한다")
        void thenReturn200WithProducts() {
          // given
          var item =
              new AffiliateSearchResponse.Product(
                  "Water",
                  12,
                  true,
                  true,
                  27664441L,
                  "https://img",
                  "탐사 소프트 3겹 롤화장지",
                  15600,
                  "https://link");
          var resp = new AffiliateSearchResponse(List.of(item));
          doReturn(resp).when(affiliateService).searchCoupangProducts(eq("Water"));

          // when
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .param("keyword", "Water")
                  .get("/api/v1/affiliate/coupang/search")
                  .then()
                  .status(HttpStatus.OK)
                  .body("productData", hasSize(1))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          queryParameters(parameterWithName("keyword").description("검색 키워드")),
                          responseFields(
                              fieldWithPath("productData").description("검색 결과 상품 목록"),
                              fieldWithPath("productData[].keyword").description("검색에 사용된 키워드"),
                              fieldWithPath("productData[].rank").description("검색 순위(노출 순서)"),
                              fieldWithPath("productData[].isRocket").description("로켓 배송 여부"),
                              fieldWithPath("productData[].isFreeShipping").description("무료 배송 여부"),
                              fieldWithPath("productData[].productId").description("상품 ID"),
                              fieldWithPath("productData[].productImage").description("상품 이미지 URL"),
                              fieldWithPath("productData[].productName").description("상품명"),
                              fieldWithPath("productData[].productPrice").description("상품 가격"),
                              fieldWithPath("productData[].productUrl").description("상품 랜딩 URL"))));

          // then
          verify(affiliateService).searchCoupangProducts("Water");

          var json = response.extract().jsonPath();
          assertThat(json.getList("productData")).hasSize(1);
          assertThat(json.getString("productData[0].keyword")).isEqualTo("Water");
          assertThat(json.getInt("productData[0].rank")).isEqualTo(12);
          assertThat(json.getBoolean("productData[0].isRocket")).isTrue();
          assertThat(json.getBoolean("productData[0].isFreeShipping")).isTrue();
          assertThat(json.getLong("productData[0].productId")).isEqualTo(27664441L);
          assertThat(json.getString("productData[0].productImage")).isEqualTo("https://img");
          assertThat(json.getString("productData[0].productName")).isEqualTo("탐사 소프트 3겹 롤화장지");
          assertThat(json.getInt("productData[0].productPrice")).isEqualTo(15600);
          assertThat(json.getString("productData[0].productUrl")).isEqualTo("https://link");
        }
      }
    }

    @Nested
    @DisplayName("Given - 결과가 비어 있을 때")
    class GivenEmptyResult {

      @Test
      @DisplayName("Then - 200과 빈 배열을 반환한다")
      void thenReturn200WithEmptyArray() {
        // given
        doReturn(new AffiliateSearchResponse(List.of()))
            .when(affiliateService)
            .searchCoupangProducts(eq("empty"));

        // when & then
        given()
            .contentType(ContentType.JSON)
            .param("keyword", "empty")
            .get("/api/v1/affiliate/coupang/search")
            .then()
            .status(HttpStatus.OK)
            .body("productData", hasSize(0))
            .apply(
                document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    queryParameters(parameterWithName("keyword").description("검색 키워드")),
                    responseFields(fieldWithPath("productData").description("검색 결과 상품 목록(빈 배열)"))));

        verify(affiliateService).searchCoupangProducts("empty");
      }
    }

    @Nested
    @DisplayName("Given - keyword 파라미터가 없는 경우")
    class GivenMissingKeyword {

      @Test
      @DisplayName("Then - 400을 반환한다")
      void thenReturn400() {
        given()
            .contentType(ContentType.JSON)
            .get("/api/v1/affiliate/coupang/search")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .apply(
                document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    queryParameters(
                        parameterWithName("keyword").description("검색 키워드").optional())));
        verifyNoInteractions(affiliateService);
      }
    }
  }
}
