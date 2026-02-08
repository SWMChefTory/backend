package com.cheftory.api;

import com.cheftory.api._common.MarketContextTestExtension;
import com.cheftory.api._common.OptimisticRetryExecutor;
import com.cheftory.api._common.region.MarketTenantIdentifierResolver;
import com.cheftory.api._config.HibernateTenantConfig;
import com.cheftory.api._config.RetryTestConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Rollback(false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ExtendWith(MarketContextTestExtension.class)
@DataJpaTest
@Import({
    MarketTenantIdentifierResolver.class,
    HibernateTenantConfig.class,
    RetryTestConfig.class,
    OptimisticRetryExecutor.class
})
public abstract class DbContextTest {}
