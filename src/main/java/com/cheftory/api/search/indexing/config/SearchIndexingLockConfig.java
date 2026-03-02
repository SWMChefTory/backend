package com.cheftory.api.search.indexing.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class SearchIndexingLockConfig {

    private final DataSource dataSource;

    @Bean
    public LockProvider lockProvider() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(jdbcTemplate)
                .usingDbTime()
                .withTableName("shedlock")
                .build());
    }
}
