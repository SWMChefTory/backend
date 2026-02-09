package com.cheftory.api.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AffiliateService 캐시 통합 테스트")
class AffiliateCacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AffiliateService service;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @MockitoBean
    private CoupangClient coupangClient;

    @Nested
    @DisplayName("캐시 저장 및 조회 테스트")
    class CacheSaveAndRetrieveTest {

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

        @Test
        @DisplayName("캐시 키가 저장된다")
        void shouldSaveCacheKeyInRedis() throws CoupangException {
            String cacheName = "coupangSearchCache";
            String keyword = "사과";

            CoupangProducts coupangProducts = mock(CoupangProducts.class);
            CoupangProduct item = mock(CoupangProduct.class);
            when(coupangProducts.getCoupangProducts()).thenReturn(List.of(item));

            when(coupangClient.searchProducts(keyword)).thenReturn(coupangProducts);

            // When - 1st call (cache miss → put)
            service.searchCoupangProducts(keyword);

            // Then - 캐시에 들어갔는지 확인
            Cache cache = cacheManager.getCache(cacheName);
            assertThat(cache).isNotNull();

            Cache.ValueWrapper cached = cache.get(keyword);
            assertThat(cached).isNotNull();
            assertThat(cached.get()).isNotNull();

            // When - 2nd call (cache hit → mock 추가 호출 없음)
            service.searchCoupangProducts(keyword);
            verify(coupangClient, times(1)).searchProducts(keyword);
        }
    }

    @Test
    @DisplayName("캐시된 데이터를 조회할 수 있다")
    void shouldRetrieveCachedDataFromCache_withDoReturnOnly() throws CoupangException {
        // Given
        String cacheName = "coupangSearchCache";
        String keyword = "배";

        // product mock
        CoupangProduct product = mock(CoupangProduct.class);
        doReturn(keyword).when(product).getKeyword();
        doReturn(1).when(product).getRank();
        doReturn(Boolean.TRUE).when(product).getIsRocket();
        doReturn(Boolean.TRUE).when(product).getIsFreeShipping();
        doReturn(200L).when(product).getProductId();
        doReturn("image.jpg").when(product).getProductImage();
        doReturn("배 상품").when(product).getProductName();
        doReturn(2000).when(product).getProductPrice();
        doReturn("url").when(product).getProductUrl();

        // wrapper mock
        CoupangProducts coupangProducts = mock(CoupangProducts.class);
        doReturn(List.of(product)).when(coupangProducts).getCoupangProducts();

        // CoupangClient mock stubbing (★ doReturn 사용)
        doReturn(coupangProducts).when(coupangClient).searchProducts(keyword);

        // When - 1st call (cache miss → put)
        service.searchCoupangProducts(keyword);

        // Then - 캐시에서 바로 확인
        Cache cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();

        Cache.ValueWrapper cached = cache.get(keyword);
        assertThat(cached).isNotNull();
        assertThat(cached.get()).isNotNull();

        // 2nd call -> cache hit (외부 호출 1회만)
        service.searchCoupangProducts(keyword);
        verify(coupangClient, times(1)).searchProducts(keyword);
    }

    @Test
    @DisplayName("여러 키워드를 각각 캐시할 수 있다")
    void shouldCacheMultipleKeywords() throws CoupangException {
        // Given
        String keyword1 = "포도";
        String keyword2 = "딸기";
        String keyword3 = "수박";

        CoupangProduct p1 = mock(CoupangProduct.class);
        CoupangProduct p2 = mock(CoupangProduct.class);
        CoupangProduct p3 = mock(CoupangProduct.class);
        doReturn(keyword1).when(p1).getKeyword();
        doReturn(keyword2).when(p2).getKeyword();
        doReturn(keyword3).when(p3).getKeyword();

        CoupangProducts w1 = mock(CoupangProducts.class);
        CoupangProducts w2 = mock(CoupangProducts.class);
        CoupangProducts w3 = mock(CoupangProducts.class);
        doReturn(List.of(p1)).when(w1).getCoupangProducts();
        doReturn(List.of(p2)).when(w2).getCoupangProducts();
        doReturn(List.of(p3)).when(w3).getCoupangProducts();

        doReturn(w1).when(coupangClient).searchProducts(keyword1);
        doReturn(w2).when(coupangClient).searchProducts(keyword2);
        doReturn(w3).when(coupangClient).searchProducts(keyword3);

        // When
        service.searchCoupangProducts(keyword1);
        service.searchCoupangProducts(keyword2);
        service.searchCoupangProducts(keyword3);

        // Then
        Cache cache = cacheManager.getCache("coupangSearchCache");
        assertThat(cache).isNotNull();
        assertThat(cache.get(keyword1)).isNotNull();
        assertThat(cache.get(keyword2)).isNotNull();
        assertThat(cache.get(keyword3)).isNotNull();
    }

    @Nested
    @DisplayName("캐시 삭제 테스트")
    class CacheEvictionTest {

        @Test
        @DisplayName("캐시를 삭제할 수 있다")
        void shouldEvictCache() throws CoupangException {
            // Given
            String keyword = "망고";

            CoupangProduct p = mock(CoupangProduct.class);
            doReturn(keyword).when(p).getKeyword();

            CoupangProducts w = mock(CoupangProducts.class);
            doReturn(List.of(p)).when(w).getCoupangProducts();
            doReturn(w).when(coupangClient).searchProducts(keyword);

            service.searchCoupangProducts(keyword);
            Cache cache = cacheManager.getCache("coupangSearchCache");
            assertThat(cache).isNotNull();
            assertThat(cache.get(keyword)).isNotNull();

            // When
            cache.evict(keyword);

            // Then
            assertThat(cache.get(keyword)).isNull();
        }

        @Test
        @DisplayName("전체 캐시를 삭제할 수 있다")
        void shouldClearAllCache() throws CoupangException {
            // Given
            String keyword1 = "키위";
            String keyword2 = "복숭아";

            CoupangProduct p1 = mock(CoupangProduct.class);
            CoupangProduct p2 = mock(CoupangProduct.class);
            doReturn(keyword1).when(p1).getKeyword();
            doReturn(keyword2).when(p2).getKeyword();

            CoupangProducts w1 = mock(CoupangProducts.class);
            CoupangProducts w2 = mock(CoupangProducts.class);
            doReturn(List.of(p1)).when(w1).getCoupangProducts();
            doReturn(List.of(p2)).when(w2).getCoupangProducts();

            doReturn(w1).when(coupangClient).searchProducts(keyword1);
            doReturn(w2).when(coupangClient).searchProducts(keyword2);

            service.searchCoupangProducts(keyword1);
            service.searchCoupangProducts(keyword2);
            Cache cache = cacheManager.getCache("coupangSearchCache");
            assertThat(cache).isNotNull();
            assertThat(cache.get(keyword1)).isNotNull();
            assertThat(cache.get(keyword2)).isNotNull();

            // When
            cache.clear();

            // Then
            assertThat(cache.get(keyword1)).isNull();
            assertThat(cache.get(keyword2)).isNull();
        }
    }

    @Nested
    @DisplayName("캐시 TTL 테스트")
    class CacheTTLTest {

        @Test
        @DisplayName("캐시에 TTL이 설정된다")
        void shouldSetTTLOnCache() throws CoupangException {
            // Given
            String keyword = "바나나";
            CoupangProduct p = mock(CoupangProduct.class);
            doReturn(keyword).when(p).getKeyword();

            CoupangProducts w = mock(CoupangProducts.class);
            doReturn(List.of(p)).when(w).getCoupangProducts();
            doReturn(w).when(coupangClient).searchProducts(keyword);

            // When
            service.searchCoupangProducts(keyword);

            // Then
            Cache cache = cacheManager.getCache("coupangSearchCache");
            assertThat(cache).isNotNull();
            assertThat(cache.get(keyword)).isNotNull();
            // (TTL의 정확한 수치를 Cache 추상화로 직접 읽을 수는 없음. 존재 여부로 간접 확인)
        }

        @Test
        @DisplayName("TTL이 지나면 캐시가 만료된다")
        void shouldExpireCacheAfterTTL() throws InterruptedException {
            // Given
            String keyword = "오렌지";
            java.time.Duration shortTTL = java.time.Duration.ofSeconds(1);

            var shortConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                            new GenericJackson2JsonRedisSerializer()))
                    .entryTtl(shortTTL)
                    .disableCachingNullValues()
                    .prefixCacheNameWith("cheftory::");

            // ★ redisTemplate 대신 주입받은 connectionFactory 사용
            CacheManager shortTTLCacheManager = RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(shortConfig)
                    .build();

            // 값 준비(서비스를 굳이 타지 않고 직접 put해도 TTL 검증엔 충분)
            CoupangProduct p = mock(CoupangProduct.class);
            doReturn(keyword).when(p).getKeyword();

            CoupangProducts w = mock(CoupangProducts.class);
            doReturn(java.util.List.of(p)).when(w).getCoupangProducts();

            // When: 짧은 TTL 캐시에 put
            Cache shortCache = shortTTLCacheManager.getCache("coupangSearchCache");
            assertThat(shortCache).isNotNull();

            shortCache.put(keyword, w);
            assertThat(shortCache.get(keyword)).isNotNull();

            Thread.sleep(1100);

            // Then
            assertThat(shortCache.get(keyword)).isNull();
        }

        @Nested
        @DisplayName("경계값 테스트")
        class BoundaryValueTest {

            @Test
            @DisplayName("빈 문자열 키워드를 캐시할 수 있다")
            void shouldCacheEmptyKeyword() throws CoupangException {
                String keyword = "";

                CoupangProduct p = mock(CoupangProduct.class);
                doReturn(keyword).when(p).getKeyword();

                CoupangProducts w = mock(CoupangProducts.class);
                doReturn(List.of(p)).when(w).getCoupangProducts();
                doReturn(w).when(coupangClient).searchProducts(keyword);

                service.searchCoupangProducts(keyword);

                Cache cache = cacheManager.getCache("coupangSearchCache");
                assertThat(cache).isNotNull();
                assertThat(cache.get(keyword)).isNotNull();
            }

            @Test
            @DisplayName("매우 긴 키워드를 캐시할 수 있다")
            void shouldCacheVeryLongKeyword() throws CoupangException {
                String keyword = "a".repeat(1000);

                CoupangProduct p = mock(CoupangProduct.class);
                doReturn(keyword).when(p).getKeyword();

                CoupangProducts w = mock(CoupangProducts.class);
                doReturn(List.of(p)).when(w).getCoupangProducts();
                doReturn(w).when(coupangClient).searchProducts(keyword);

                service.searchCoupangProducts(keyword);

                Cache cache = cacheManager.getCache("coupangSearchCache");
                assertThat(cache).isNotNull();
                assertThat(cache.get(keyword)).isNotNull();
            }

            @Test
            @DisplayName("존재하지 않는 키를 조회하면 null을 반환한다")
            void shouldReturnNullForNonExistentKey() {
                String keyword = "존재하지않는키워드";
                Cache cache = cacheManager.getCache("coupangSearchCache");
                assertThat(cache).isNotNull();
                assertThat(cache.get(keyword)).isNull();
            }
        }
    }
}
