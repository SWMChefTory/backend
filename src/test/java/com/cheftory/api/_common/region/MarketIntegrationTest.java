package com.cheftory.api._common.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api._config.AsyncConfig;
import com.cheftory.api._config.HibernateTenantConfig;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.GlobalErrorCode;
import jakarta.persistence.EntityManager;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import({TestRegionService.class, MarketTenantIdentifierResolver.class, HibernateTenantConfig.class, AsyncConfig.class})
class MarketIntegrationTest {

    @Autowired
    TestRegionService regionService;

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    EntityManager em;

    @AfterEach
    void tearDown() {
        new TransactionTemplate(txManager).execute(status -> {
            em.createNativeQuery("DELETE FROM test_region_entity").executeUpdate();
            return null;
        });

        // 테스트가 잘 짜이면 항상 null이어야 함
        assertThat(MarketContext.currentOrNull()).isNull();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void async_create_should_be_isolated_by_market() {
        withMarket(Market.KOREA, "KR", () -> regionService.createAsync("korea").get(3, TimeUnit.SECONDS));
        withMarket(
                Market.GLOBAL, "US", () -> regionService.createAsync("global").get(3, TimeUnit.SECONDS));

        withMarket(Market.KOREA, "KR", () -> assertThat(regionService.findAllNames())
                .allMatch(n -> n.startsWith("korea")));

        withMarket(Market.GLOBAL, "US", () -> assertThat(regionService.findAllNames())
                .allMatch(n -> n.startsWith("global")));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void async_without_marketContext_should_throw_unknown_region() {
        var f = regionService.createAsync("x");

        assertThatThrownBy(() -> f.get(3, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasRootCauseInstanceOf(CheftoryException.class)
                .rootCause()
                .isInstanceOfSatisfying(CheftoryException.class, ex -> assertThat(ex.getError())
                        .isEqualTo(GlobalErrorCode.UNKNOWN_REGION));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void should_filter_by_market() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(Propagation.REQUIRES_NEW.value());

        withMarket(
                Market.KOREA,
                "KR",
                () -> tx.execute(status -> {
                    regionService.save("korea");
                    return null;
                }));

        withMarket(
                Market.GLOBAL,
                "US",
                () -> tx.execute(status -> {
                    regionService.save("global");
                    return null;
                }));

        withMarket(
                Market.KOREA,
                "KR",
                () -> tx.execute(status -> {
                    assertThat(regionService.findAllNames()).containsExactly("korea");
                    return null;
                }));

        withMarket(
                Market.GLOBAL,
                "US",
                () -> tx.execute(status -> {
                    assertThat(regionService.findAllNames()).containsExactly("global");
                    return null;
                }));
    }

    private static void withMarket(Market market, String countryCode, ThrowingRunnable r) {
        try (var ignored = MarketContext.with(new MarketContext.Info(market, countryCode))) {
            r.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
