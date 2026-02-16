package com.cheftory.api.ranking.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataRedisTest
@ActiveProfiles("test")
@DisplayName("RankingSnapshotRepository Tests")
class RankingSnapshotRepositoryTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private RankingSnapshotRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RankingSnapshotRepository(redisTemplate);
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    @DisplayName("save/get")
    class SaveAndGet {

        @Test
        @DisplayName("should save and get string")
        void shouldSaveAndGetString() {
            repository.saveString("key", "value", Duration.ofMinutes(1));

            assertThat(repository.getString("key")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("expire")
    class Expire {

        @Test
        @DisplayName("should set ttl on key")
        void shouldSetTtlOnKey() {
            repository.saveString("key", "value", Duration.ofMinutes(5));
            repository.expire("key", Duration.ofSeconds(10));

            Long ttl = redisTemplate.getExpire("key", TimeUnit.SECONDS);
            assertThat(ttl).isPositive();
        }
    }

    @Nested
    @DisplayName("incrementLong")
    class IncrementLong {

        @Test
        @DisplayName("should increment value")
        void shouldIncrementValue() {
            Long value = repository.incrementLong("counter", 3);

            assertThat(value).isEqualTo(3L);
            assertThat(repository.getString("counter")).isEqualTo("3");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete key")
        void shouldDeleteKey() {
            repository.saveString("key", "value", Duration.ofMinutes(1));

            repository.delete("key");

            assertThat(repository.getString("key")).isNull();
        }
    }
}
