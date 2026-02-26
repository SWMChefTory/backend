package com.cheftory.api.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cheftory.api._support.TestKeyNamespaceSupport;
import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProducts;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ContextConfiguration(classes = AffiliateCacheTest.TestConfig.class)
@DisplayName("AffiliateService 캐시 테스트")
class AffiliateCacheTest extends TestKeyNamespaceSupport {

    @TestConfiguration
    @EnableCaching
    static class TestConfig {
        @Bean
        AffiliateService affiliateService(CoupangClient coupangClient) {
            return new AffiliateService(coupangClient);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("coupangSearchCache");
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AffiliateService service;

    @MockitoBean
    private CoupangClient coupangClient;

    @Nested
    @DisplayName("캐시 저장 및 조회 (searchCoupangProducts)")
    class CacheSaveAndRetrieve {

        @Test
        @DisplayName("같은 키워드를 두 번 조회하면 외부 호출은 한 번만 발생한다")
        void cachesByKeyword() throws CoupangException {
            String keyword = uniqueKeyword("사과");
            CoupangProducts products = emptyProducts();
            doReturn(products).when(coupangClient).searchProducts(keyword);

            CoupangProducts first = service.searchCoupangProducts(keyword);
            CoupangProducts second = service.searchCoupangProducts(keyword);

            assertThat(first).isNotNull();
            assertThat(second).isNotNull();
            verify(coupangClient, times(1)).searchProducts(keyword);

            Cache cache = cacheManager.getCache("coupangSearchCache");
            assertThat(cache).isNotNull();
            assertThat(cache.get(keyword)).isNotNull();
        }

        @Test
        @DisplayName("서로 다른 키워드는 각각 캐시된다")
        void cachesPerKeywordIndependently() throws CoupangException {
            String keyword1 = uniqueKeyword("포도");
            String keyword2 = uniqueKeyword("딸기");
            String keyword3 = uniqueKeyword("수박");

            doReturn(emptyProducts()).when(coupangClient).searchProducts(keyword1);
            doReturn(emptyProducts()).when(coupangClient).searchProducts(keyword2);
            doReturn(emptyProducts()).when(coupangClient).searchProducts(keyword3);

            service.searchCoupangProducts(keyword1);
            service.searchCoupangProducts(keyword2);
            service.searchCoupangProducts(keyword3);

            Cache cache = cacheManager.getCache("coupangSearchCache");
            assertThat(cache).isNotNull();
            assertThat(cache.get(keyword1)).isNotNull();
            assertThat(cache.get(keyword2)).isNotNull();
            assertThat(cache.get(keyword3)).isNotNull();
        }
    }

    @Nested
    @DisplayName("캐시 삭제 (evict)")
    class CacheEviction {

        @Test
        @DisplayName("키를 제거한 뒤 동일 키를 조회하면 외부 호출이 다시 발생한다")
        void refetchesAfterEvict() throws CoupangException {
            String keyword = uniqueKeyword("망고");
            Cache cache = cacheManager.getCache("coupangSearchCache");
            assertThat(cache).isNotNull();

            doReturn(emptyProducts()).when(coupangClient).searchProducts(keyword);

            service.searchCoupangProducts(keyword);
            cache.evictIfPresent(keyword);
            clearInvocations(coupangClient);

            service.searchCoupangProducts(keyword);

            verify(coupangClient, times(1)).searchProducts(keyword);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValues {

        @Test
        @DisplayName("공백 포함 키워드도 검색할 수 있다")
        void realWorldKeywordWithWhitespace() throws CoupangException {
            String keyword = uniqueKeyword("닭가슴살 샐러드");
            doReturn(emptyProducts()).when(coupangClient).searchProducts(keyword);

            CoupangProducts result = service.searchCoupangProducts(keyword);

            assertThat(result).isNotNull();
            verify(coupangClient, times(1)).searchProducts(keyword);
        }

        @Test
        @DisplayName("존재하지 않는 키 조회 시 null을 반환한다")
        void nonExistentKey() {
            String keyword = uniqueKeyword("존재하지않는키워드");
            Cache cache = cacheManager.getCache("coupangSearchCache");

            assertThat(cache).isNotNull();
            assertThat(cache.get(keyword)).isNull();
        }
    }

    @Nested
    @DisplayName("예외 전파")
    class ExceptionPropagation {

        @Test
        @DisplayName("외부 클라이언트 예외는 그대로 전파된다")
        void propagatesCoupangException() throws CoupangException {
            String keyword = uniqueKeyword("예외키워드");
            CoupangException exception = new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
            doThrow(exception).when(coupangClient).searchProducts(keyword);

            assertThatCode(() -> service.searchCoupangProducts(keyword)).isInstanceOf(CoupangException.class);
        }
    }

    private String uniqueKeyword(String baseKeyword) {
        return key("affiliate-cache-test:" + baseKeyword);
    }

    private CoupangProducts emptyProducts() {
        return CoupangProducts.of(new ArrayList<>());
    }
}
