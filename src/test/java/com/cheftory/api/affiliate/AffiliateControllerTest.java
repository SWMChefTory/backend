package com.cheftory.api.affiliate;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

@DisplayName("AffiliateController 테스트")
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
    @DisplayName("쿠팡 상품 검색 (searchCoupangProducts)")
    class SearchCoupangProducts {

        @Nested
        @DisplayName("Given - 유효한 검색어가 주어졌을 때")
        class GivenValidKeyword {
            String keyword;
            CoupangProducts wrapper;

            @BeforeEach
            void setUp() throws CoupangException {
                keyword = "Water";
                CoupangProduct item = mock(CoupangProduct.class);
                doReturn("Water").when(item).getKeyword();
                doReturn(12).when(item).getRank();
                doReturn(Boolean.TRUE).when(item).getIsRocket();
                doReturn(Boolean.TRUE).when(item).getIsFreeShipping();
                doReturn(27664441L).when(item).getProductId();
                doReturn("https://img").when(item).getProductImage();
                doReturn("탐사 소프트 3겹 롤화장지").when(item).getProductName();
                doReturn(15600).when(item).getProductPrice();
                doReturn("https://link").when(item).getProductUrl();

                wrapper = mock(CoupangProducts.class);
                doReturn(List.of(item)).when(wrapper).getCoupangProducts();
                doReturn(wrapper).when(affiliateService).searchCoupangProducts(any(String.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .param("keyword", keyword)
                            .get("/api/v1/affiliate/search/coupang")
                            .then();
                }

                @Test
                @DisplayName("Then - 상품 목록을 반환한다")
                void thenReturnsProducts() throws CoupangException {
                    response.status(HttpStatus.OK)
                            .body("coupangProducts.coupangProducts", hasSize(1))
                            .apply(document(
                                    getNestedClassPath(AffiliateControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    queryParameters(parameterWithName("keyword").description("검색 키워드")),
                                    responseFields(
                                            fieldWithPath("coupangProducts.coupangProducts")
                                                    .description("검색 결과 상품 목록"),
                                            fieldWithPath("coupangProducts.coupangProducts[].keyword")
                                                    .description("검색에 사용된 키워드"),
                                            fieldWithPath("coupangProducts.coupangProducts[].rank")
                                                    .description("검색 순위(노출 순서)"),
                                            fieldWithPath("coupangProducts.coupangProducts[].isRocket")
                                                    .description("로켓 배송 여부"),
                                            fieldWithPath("coupangProducts.coupangProducts[].isFreeShipping")
                                                    .description("무료 배송 여부"),
                                            fieldWithPath("coupangProducts.coupangProducts[].productId")
                                                    .description("상품 ID"),
                                            fieldWithPath("coupangProducts.coupangProducts[].productImage")
                                                    .description("상품 이미지 URL"),
                                            fieldWithPath("coupangProducts.coupangProducts[].productName")
                                                    .description("상품명"),
                                            fieldWithPath("coupangProducts.coupangProducts[].productPrice")
                                                    .description("상품 가격"),
                                            fieldWithPath("coupangProducts.coupangProducts[].productUrl")
                                                    .description("상품 랜딩 URL"))));

                    verify(affiliateService).searchCoupangProducts(keyword);

                    var json = response.extract().jsonPath();
                    assertThat(json.getList("coupangProducts.coupangProducts")).hasSize(1);
                    String base = "coupangProducts.coupangProducts[0].";
                    assertThat(json.getString(base + "keyword")).isEqualTo("Water");
                    assertThat(json.getInt(base + "rank")).isEqualTo(12);
                    assertThat(json.getBoolean(base + "isRocket")).isTrue();
                    assertThat(json.getBoolean(base + "isFreeShipping")).isTrue();
                    assertThat(json.getLong(base + "productId")).isEqualTo(27664441L);
                    assertThat(json.getString(base + "productImage")).isEqualTo("https://img");
                    assertThat(json.getString(base + "productName")).isEqualTo("탐사 소프트 3겹 롤화장지");
                    assertThat(json.getInt(base + "productPrice")).isEqualTo(15600);
                    assertThat(json.getString(base + "productUrl")).isEqualTo("https://link");
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색 결과가 없을 때")
        class GivenEmptyResult {
            String keyword;

            @BeforeEach
            void setUp() throws CoupangException {
                keyword = "empty";
                CoupangProducts emptyWrapper = mock(CoupangProducts.class);
                doReturn(List.of()).when(emptyWrapper).getCoupangProducts();
                doReturn(emptyWrapper).when(affiliateService).searchCoupangProducts(keyword);
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .param("keyword", keyword)
                            .get("/api/v1/affiliate/search/coupang")
                            .then();
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() throws CoupangException {
                    response.status(HttpStatus.OK)
                            .body("coupangProducts.coupangProducts", hasSize(0))
                            .apply(document(
                                    getNestedClassPath(AffiliateControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    queryParameters(parameterWithName("keyword").description("검색 키워드")),
                                    responseFields(fieldWithPath("coupangProducts.coupangProducts")
                                            .description("검색 결과 상품 목록(빈 배열)"))));

                    verify(affiliateService).searchCoupangProducts(keyword);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검색어가 누락되었을 때")
        class GivenMissingKeyword {

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenSearching {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .get("/api/v1/affiliate/search/coupang")
                            .then();
                }

                @Test
                @DisplayName("Then - 400 Bad Request를 반환한다")
                void thenReturnsBadRequest() {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(AffiliateControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    queryParameters(parameterWithName("keyword")
                                            .description("검색 키워드")
                                            .optional())));
                    verifyNoInteractions(affiliateService);
                }
            }
        }
    }
}
