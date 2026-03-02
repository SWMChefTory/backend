package com.cheftory.api.search.indexing.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.search.indexing.config.SearchIndexingLockConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@Import(SearchIndexingLockConfig.class)
class SearchIndexingLockConfigTest {

    private static final String CREATE_SHEDLOCK_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS shedlock (
                name VARCHAR(64) NOT NULL PRIMARY KEY,
                lock_until TIMESTAMP(3) NOT NULL,
                locked_at TIMESTAMP(3) NOT NULL,
                locked_by VARCHAR(255) NOT NULL
            )
            """;

    @Autowired
    private LockProvider lockProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(CREATE_SHEDLOCK_TABLE_SQL);
        jdbcTemplate.update("DELETE FROM shedlock");
    }

    @Test
    @DisplayName("ShedLock 테이블이 생성되고 동일한 락 이름의 중복 획득을 막는다")
    void shouldCreateShedLockTableAndBlockDuplicateLock() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM shedlock", Integer.class);
        assertThat(count).isNotNull();

        LockConfiguration lockConfiguration = new LockConfiguration(
                Instant.now(), "search-indexing-query-upsert", Duration.ofMinutes(1), Duration.ZERO);

        Optional<SimpleLock> firstLock = lockProvider.lock(lockConfiguration);
        assertThat(firstLock).isPresent();

        Optional<SimpleLock> secondLock = lockProvider.lock(new LockConfiguration(
                Instant.now(), "search-indexing-query-upsert", Duration.ofMinutes(1), Duration.ZERO));
        assertThat(secondLock).isEmpty();

        firstLock.orElseThrow().unlock();

        Optional<SimpleLock> lockAfterUnlock = lockProvider.lock(new LockConfiguration(
                Instant.now(), "search-indexing-query-upsert", Duration.ofMinutes(1), Duration.ZERO));
        assertThat(lockAfterUnlock).isPresent();
        lockAfterUnlock.orElseThrow().unlock();
    }
}
