package com.cheftory.api._config;

import com.cheftory.api._common.region.MarketTenantIdentifierResolver;
import java.util.Map;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateTenantConfig {

  @Bean
  public HibernatePropertiesCustomizer tenantIdResolverCustomizer(
      MarketTenantIdentifierResolver resolver) {
    return (Map<String, Object> props) ->
        props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
  }
}
