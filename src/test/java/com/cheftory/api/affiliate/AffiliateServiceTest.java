package com.cheftory.api.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;
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

            private CoupangProducts coupangProducts;
            private CoupangProduct coupangProduct;

            @BeforeEach
            void setUp() {
                coupangProducts = mock(CoupangProducts.class);
                coupangProduct = mock(CoupangProduct.class);
                doReturn("Water").when(coupangProduct).getKeyword();
                doReturn(12).when(coupangProduct).getRank();
                doReturn(Boolean.TRUE).when(coupangProduct).getIsRocket();
                doReturn(Boolean.TRUE).when(coupangProduct).getIsFreeShipping();
                doReturn(27664441L).when(coupangProduct).getProductId();
                doReturn("https://ads-partners.coupang.com/image1/abc.png")
                        .when(coupangProduct)
                        .getProductImage();
                doReturn("탐사 소프트 3겹 롤화장지").when(coupangProduct).getProductName();
                doReturn(15600).when(coupangProduct).getProductPrice();
                doReturn("https://link.coupang.com/re/AFFSDP?...")
                        .when(coupangProduct)
                        .getProductUrl();

                List<CoupangProduct> coupangProductList = List.of(coupangProduct);
                doReturn(coupangProductList).when(coupangProducts).getCoupangProducts();
            }

            @Nested
            @DisplayName("When - 1개 상품을 검색하면")
            class WhenOneProduct {

                @Test
                @DisplayName("Then - CoupangClient의 응답을 그대로 반환한다")
                void thenReturnProductList() {
                    doReturn(coupangProducts).when(coupangClient).searchProducts("Water");

                    CoupangProducts products = service.searchCoupangProducts("Water");

                    assertThat(products).isNotNull();
                    assertThat(products.getCoupangProducts()).hasSize(1);

                    var item = products.getCoupangProducts().getFirst();
                    assertThat(item.getKeyword()).isEqualTo("Water");
                    assertThat(item.getRank()).isEqualTo(12);
                    assertThat(item.getIsRocket()).isTrue();
                    assertThat(item.getIsFreeShipping()).isTrue();
                    assertThat(item.getProductId()).isEqualTo(27664441L);
                    assertThat(item.getProductImage()).isEqualTo("https://ads-partners.coupang.com/image1/abc.png");
                    assertThat(item.getProductName()).isEqualTo("탐사 소프트 3겹 롤화장지");
                    assertThat(item.getProductPrice()).isEqualTo(15600);
                    assertThat(item.getProductUrl()).isEqualTo("https://link.coupang.com/re/AFFSDP?...");

                    verify(coupangClient, times(1)).searchProducts(eq("Water"));
                }
            }
        }
    }
}
