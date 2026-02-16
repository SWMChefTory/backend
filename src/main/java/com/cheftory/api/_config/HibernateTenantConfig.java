package com.cheftory.api._config;

import com.cheftory.api._common.region.MarketTenantIdentifierResolver;
import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hibernate 멀티테넌트 설정 클래스.
 */
@Configuration
public class HibernateTenantConfig {

    /**
     * 멀티테넌트 식별자 리졸버를 설정합니다.
     *
     * @param resolver 마켓 테넌트 식별자 리졸버
     * @return HibernatePropertiesCustomizer 인스턴스
     */
    @Bean
    public HibernatePropertiesCustomizer tenantIdResolverCustomizer(MarketTenantIdentifierResolver resolver) {
        return (Map<String, Object> props) -> props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
