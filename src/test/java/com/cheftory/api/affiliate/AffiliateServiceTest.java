package com.cheftory.api.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import com.cheftory.api.affiliate.dto.AffiliateSearchResponse;
import java.util.List;
import org.junit.jupiter.api.*;

@DisplayName("AffiliateService")
class AffiliateServiceTest {

  private AffiliateService service;
  private CoupangClient coupangClient;

  @BeforeEach
  void setUp() {
    coupangClient = mock(CoupangClient.class);
    service = new AffiliateService(coupangClient);
  }

  @Nested
  @DisplayName("searchCoupangProducts(keyword)")
  class SearchCoupangProducts {

    @Nested
    @DisplayName("Given - CoupangClient가 정상 응답을 줄 때")
    class GivenValidResponse {

      @Nested
      @DisplayName("When - 1개 상품을 검색하면")
      class WhenOneProduct {

        @Test
        @DisplayName("Then - 상품이 올바르게 매핑되어 반환된다")
        void thenMapSingleProduct() {
          var p =
              new CoupangSearchResponse.Product(
                  "Water",
                  12,
                  Boolean.TRUE,
                  Boolean.TRUE,
                  27664441L,
                  "https://ads-partners.coupang.com/image1/abc.png",
                  "탐사 소프트 3겹 롤화장지",
                  15600,
                  "https://link.coupang.com/re/AFFSDP?...");
          var data =
              new CoupangSearchResponse.Data("https://link.coupang.com/re/AFFSRP?....", List.of(p));
          var res = new CoupangSearchResponse("0", "", data);

          when(coupangClient.searchProducts("Water")).thenReturn(res);

          AffiliateSearchResponse out = service.searchCoupangProducts("Water");

          assertThat(out).isNotNull();
          assertThat(out.productData()).hasSize(1);

          var item = out.productData().get(0);
          assertThat(item.keyword()).isEqualTo("Water");
          assertThat(item.rank()).isEqualTo(12);
          assertThat(item.isRocket()).isTrue(); // Boolean.TRUE.equals → true
          assertThat(item.isFreeShipping()).isTrue(); // Boolean.TRUE.equals → true
          assertThat(item.productId()).isEqualTo(27664441L);
          assertThat(item.productImage())
              .isEqualTo("https://ads-partners.coupang.com/image1/abc.png");
          assertThat(item.productName()).isEqualTo("탐사 소프트 3겹 롤화장지");
          assertThat(item.productPrice()).isEqualTo(15600);
          assertThat(item.productUrl()).isEqualTo("https://link.coupang.com/re/AFFSDP?...");

          verify(coupangClient, times(1)).searchProducts(eq("Water"));
        }
      }

      @Nested
      @DisplayName("When - 여러 상품을 검색하면")
      class WhenMultipleProducts {

        @Test
        @DisplayName("Then - 모든 상품이 순서대로 매핑된다")
        void thenMapMultipleProducts() {
          var p1 =
              new CoupangSearchResponse.Product(
                  "키보드", 1, true, null, 1L, "img1", "name1", 1000, "url1");
          var p2 =
              new CoupangSearchResponse.Product(
                  "마우스", 2, null, true, 2L, "img2", "name2", 2000, "url2");
          var res =
              new CoupangSearchResponse(
                  "0", "", new CoupangSearchResponse.Data("landing", List.of(p1, p2)));

          when(coupangClient.searchProducts("it")).thenReturn(res);

          var out = service.searchCoupangProducts("it");

          assertThat(out.productData()).hasSize(2);
          assertThat(out.productData().get(0).keyword()).isEqualTo("키보드");
          assertThat(out.productData().get(1).keyword()).isEqualTo("마우스");
          // null Boolean → false 매핑 확인
          assertThat(out.productData().get(0).isFreeShipping()).isFalse();
          assertThat(out.productData().get(1).isRocket()).isFalse();

          verify(coupangClient).searchProducts("it");
        }
      }
    }

    @Nested
    @DisplayName("Given - 응답의 특정 필드가 null일 때")
    class GivenNullCases {

      @Test
      @DisplayName("Then - Boolean 필드가 null이면 false로 매핑된다")
      void thenNullBooleansBecomeFalse() {
        var p =
            new CoupangSearchResponse.Product("키워드", 3, null, null, 10L, "img", "name", 999, "url");
        var res =
            new CoupangSearchResponse(
                "0", "", new CoupangSearchResponse.Data("landing", List.of(p)));
        when(coupangClient.searchProducts("k")).thenReturn(res);

        var out = service.searchCoupangProducts("k");

        assertThat(out.productData()).hasSize(1);
        var item = out.productData().get(0);
        assertThat(item.isRocket()).isFalse();
        assertThat(item.isFreeShipping()).isFalse();
      }

      @Test
      @DisplayName("Then - CoupangSearchResponse가 null이면 빈 리스트를 반환한다")
      void thenNullResponseReturnsEmpty() {
        when(coupangClient.searchProducts("any")).thenReturn(null);

        var out = service.searchCoupangProducts("any");

        assertThat(out.productData()).isEmpty();
      }

      @Test
      @DisplayName("Then - data가 null이면 빈 리스트를 반환한다")
      void thenNullDataReturnsEmpty() {
        var res = new CoupangSearchResponse("0", "", null);
        when(coupangClient.searchProducts("x")).thenReturn(res);

        var out = service.searchCoupangProducts("x");

        assertThat(out.productData()).isEmpty();
      }

      @Test
      @DisplayName("Then - productData가 null이면 빈 리스트를 반환한다")
      void thenNullProductDataReturnsEmpty() {
        var data = new CoupangSearchResponse.Data("landing", null);
        var res = new CoupangSearchResponse("0", "", data);
        when(coupangClient.searchProducts("y")).thenReturn(res);

        var out = service.searchCoupangProducts("y");

        assertThat(out.productData()).isEmpty();
      }

      @Test
      @DisplayName("Then - productData가 빈 리스트면 그대로 빈 리스트를 반환한다")
      void thenEmptyProductDataReturnsEmpty() {
        var data = new CoupangSearchResponse.Data("landing", List.of());
        var res = new CoupangSearchResponse("0", "", data);
        when(coupangClient.searchProducts("z")).thenReturn(res);

        var out = service.searchCoupangProducts("z");

        assertThat(out.productData()).isEmpty();
      }
    }
  }
}
