package com.cheftory.api.market;

import static com.cheftory.api._common.region.MarketContext.with;
import static org.hamcrest.Matchers.equalTo;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.exception.GlobalErrorCode;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("MarketController 테스트")
class MarketControllerTest extends RestDocsTest {

    private MarketController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        controller = new MarketController();
        exceptionHandler = new GlobalExceptionHandler();
        mockMvc = mockMvcBuilder(controller).withAdvice(exceptionHandler).build();
    }

    @AfterEach
    void tearDown() {
        MarketContext.currentOrNull();
    }

    @Nested
    @DisplayName("GET /api/v1/market")
    class GetMarket {

        @Nested
        @DisplayName("Given - MarketContext가 설정된 경우")
        class GivenMarketContextPresent {
            @Test
            @DisplayName("Then - market과 country_code를 반환한다")
            void thenReturnsMarketInfo() throws Exception {
                try (var ignored = with(new MarketContext.Info(Market.KOREA, "KR"))) {
                    given().contentType(ContentType.JSON)
                            .when()
                            .get("/api/v1/market")
                            .then()
                            .status(HttpStatus.OK)
                            .body("market", equalTo("KOREA"))
                            .body("country_code", equalTo("KR"));
                }
            }
        }

        @Nested
        @DisplayName("Given - MarketContext가 설정되지 않은 경우")
        class GivenMarketContextMissing {

            @AfterEach
            void clearContext() {
                with(null);
            }

            @Test
            @DisplayName("Then - UNKNOWN_REGION 에러를 반환한다")
            void thenReturnsUnknownRegionError() {
                var response = given().contentType(ContentType.JSON)
                        .when()
                        .get("/api/v1/market")
                        .then();

                assertErrorResponse(response, GlobalErrorCode.UNKNOWN_REGION);
            }
        }
    }
}
