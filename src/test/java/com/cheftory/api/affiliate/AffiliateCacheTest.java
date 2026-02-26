package com.cheftory.api.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import com.cheftory.api._support.RedisTemplateTestSupport;
import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProducts;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("AffiliateService 캐시 통합 테스트")
class AffiliateCacheTest extends RedisTemplateTestSupport {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AffiliateService service;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @MockitoBean
    private CoupangClient coupangClient;

    @Nested
    @DisplayName("캐시 저장 및 조회 (searchCoupangProducts)")
    class CacheSaveAndRetrieve {

        @Nested
        @DisplayName("Given - 새로운 키워드로 검색할 때")
        class GivenNewKeyword {
            String cacheName;
            String keyword;
            CoupangProducts coupangProducts;

            @BeforeEach
            void setUp() throws CoupangException {
                cacheName = "coupangSearchCache";
                keyword = uniqueKeyword("사과");
                coupangProducts = emptyProducts();
                when(coupangClient.searchProducts(keyword)).thenReturn(coupangProducts);
            }

            @Nested
            @DisplayName("When - 첫 번째 검색을 요청하면")
            class WhenFirstSearch {

                @BeforeEach
                void setUp() throws CoupangException {
                    service.searchCoupangProducts(keyword);
                    service.searchCoupangProducts(keyword);
                }

                @Test
                @DisplayName("Then - 캐시에 저장된다")
                void thenSavesToCache() {
                    Cache cache = cacheManager.getCache(cacheName);
                    assertThat(cache).isNotNull();
                    Cache.ValueWrapper cached = cache.get(keyword);
                    assertThat(cached).isNotNull();
                    assertThat(cached.get()).isNotNull();
                }
            }

            @Nested
            @DisplayName("When - 두 번째 검색을 요청하면")
            class WhenSecondSearch {

                @BeforeEach
                void setUp() throws CoupangException {
                    service.searchCoupangProducts(keyword);
                }

                @Test
                @DisplayName("Then - 캐시에서 조회하고 외부 호출은 하지 않는다")
                void thenRetrievesFromCache() throws CoupangException {
                    verify(coupangClient, times(1)).searchProducts(keyword);
                }
            }
        }

        @Nested
        @DisplayName("Given - 여러 키워드로 검색할 때")
        class GivenMultipleKeywords {
            String keyword1;
            String keyword2;
            String keyword3;

            @BeforeEach
            void setUp() throws CoupangException {
                keyword1 = uniqueKeyword("포도");
                keyword2 = uniqueKeyword("딸기");
                keyword3 = uniqueKeyword("수박");

                CoupangProducts w1 = emptyProducts();
                CoupangProducts w2 = emptyProducts();
                CoupangProducts w3 = emptyProducts();

                doReturn(w1).when(coupangClient).searchProducts(keyword1);
                doReturn(w2).when(coupangClient).searchProducts(keyword2);
                doReturn(w3).when(coupangClient).searchProducts(keyword3);
            }

            @Nested
            @DisplayName("When - 각각 검색을 요청하면")
            class WhenSearchingAll {

                @BeforeEach
                void setUp() throws CoupangException {
                    service.searchCoupangProducts(keyword1);
                    service.searchCoupangProducts(keyword2);
                    service.searchCoupangProducts(keyword3);
                }

                @Test
                @DisplayName("Then - 모든 키워드가 캐시된다")
                void thenAllCached() {
                    Cache cache = cacheManager.getCache("coupangSearchCache");
                    assertThat(cache).isNotNull();
                    assertThat(cache.get(keyword1)).isNotNull();
                    assertThat(cache.get(keyword2)).isNotNull();
                    assertThat(cache.get(keyword3)).isNotNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("캐시 삭제 (evict)")
    class CacheEviction {

        @Nested
        @DisplayName("Given - 캐시된 데이터가 있을 때")
        class GivenCachedData {
            String keyword;
            Cache cache;

            @BeforeEach
            void setUp() throws CoupangException {
                keyword = uniqueKeyword("망고");
                CoupangProducts w = emptyProducts();
                doReturn(w).when(coupangClient).searchProducts(keyword);
                service.searchCoupangProducts(keyword);
                cache = cacheManager.getCache("coupangSearchCache");
            }

            @Nested
            @DisplayName("When - 특정 키 삭제를 요청하면")
            class WhenEvictingKey {

                @BeforeEach
                void setUp() {
                    cache.evictIfPresent(keyword);
                    clearInvocations(coupangClient);
                }

                @Test
                @DisplayName("Then - 이후 동일 키 재조회 시 외부 호출이 다시 발생한다")
                void thenEvicted() throws CoupangException {
                    assertThatCode(() -> service.searchCoupangProducts(keyword)).doesNotThrowAnyException();
                    verify(coupangClient, times(1)).searchProducts(keyword);
                }
            }
        }
    }

    @Nested
    @DisplayName("캐시 만료 (TTL)")
    class CacheTTL {

        @Nested
        @DisplayName("Given - 짧은 TTL이 설정된 캐시가 있을 때")
        class GivenShortTTL {
            CacheManager shortTTLCacheManager;
            String keyword;
            CoupangProducts products;

            @BeforeEach
            void setUp() {
                keyword = uniqueKeyword("오렌지");
                products = emptyProducts();
                java.time.Duration shortTTL = java.time.Duration.ofSeconds(1);
                ObjectMapper om = new ObjectMapper();
                var shortConfig = RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJacksonJsonRedisSerializer(om)))
                        .entryTtl(shortTTL)
                        .disableCachingNullValues()
                        .prefixCacheNameWith("cheftory::");

                shortTTLCacheManager = RedisCacheManager.builder(connectionFactory)
                        .cacheDefaults(shortConfig)
                        .build();
            }

            @Nested
            @DisplayName("When - 데이터를 저장하고 TTL 시간이 지나면")
            class WhenTimePasses {

                @BeforeEach
                void setUp() throws InterruptedException {
                    Cache shortCache = shortTTLCacheManager.getCache("coupangSearchCache");
                    shortCache.put(keyword, products);
                    Thread.sleep(1100);
                }

                @Test
                @DisplayName("Then - 캐시가 만료되어 조회되지 않는다")
                void thenExpired() {
                    Cache shortCache = shortTTLCacheManager.getCache("coupangSearchCache");
                    assertThat(shortCache.get(keyword)).isNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValues {

        @Nested
        @DisplayName("Given - 특수한 키워드가 주어졌을 때")
        class GivenSpecialKeywords {

            @Test
            @DisplayName("Then - 실제 검색어 형태(공백 포함)도 검색할 수 있다")
            void realWorldKeywordWithWhitespace() throws CoupangException {
                String keyword = uniqueKeyword("닭가슴살 샐러드");
                CoupangProducts w = emptyProducts();
                doReturn(w).when(coupangClient).searchProducts(keyword);

                CoupangProducts result = service.searchCoupangProducts(keyword);

                assertThat(result).isNotNull();
                verify(coupangClient, times(1)).searchProducts(keyword);
            }

            @Test
            @DisplayName("Then - 존재하지 않는 키 조회 시 null을 반환한다")
            void nonExistentKey() {
                String keyword = uniqueKeyword("존재하지않는키워드");
                Cache cache = cacheManager.getCache("coupangSearchCache");
                assertThat(cache.get(keyword)).isNull();
            }
        }
    }

    private String uniqueKeyword(String baseKeyword) {
        return key("affiliate-cache-test:" + baseKeyword);
    }

    private CoupangProducts emptyProducts() {
        return CoupangProducts.of(new ArrayList<>());
    }
}
