package com.cheftory.api.ranking.interaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
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
@DisplayName("RankingInteractionRepository Tests")
class RankingInteractionRepositoryTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private RankingInteractionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RankingInteractionRepository(redisTemplate);
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    @DisplayName("add and getLatest")
    class AddAndGetLatest {

        @Test
        @DisplayName("should return latest items by score")
        void shouldReturnLatestItemsByScore() {
            String key = "ranking:recent";
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            repository.add(key, id1, 1000L);
            repository.add(key, id2, 2000L);
            repository.add(key, id3, 3000L);

            List<UUID> result = repository.getLatest(key, 2);

            assertThat(result).containsExactly(id3, id2);
        }
    }

    @Nested
    @DisplayName("pruneBefore")
    class PruneBefore {

        @Test
        @DisplayName("should remove entries before the cutoff")
        void shouldRemoveEntriesBeforeCutoff() {
            String key = "ranking:seen";
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            repository.add(key, id1, 1000L);
            repository.add(key, id2, 2000L);
            repository.add(key, id3, 3000L);

            repository.pruneBefore(key, 2500L);

            List<UUID> result = repository.getLatest(key, 10);
            assertThat(result).containsExactly(id3);
        }
    }

    @Nested
    @DisplayName("expire")
    class Expire {

        @Test
        @DisplayName("should expire the key")
        void shouldExpireKey() throws InterruptedException {
            String key = "ranking:recent";
            UUID id = UUID.randomUUID();

            repository.add(key, id, 1000L);
            repository.expire(key, Duration.ofSeconds(1));

            List<UUID> before = repository.getLatest(key, 10);
            assertThat(before).contains(id);

            Thread.sleep(1100);

            List<UUID> after = repository.getLatest(key, 10);
            assertThat(after).isEmpty();
        }
    }
}
