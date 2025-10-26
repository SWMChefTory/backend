package com.cheftory.api.recipeinfo.search.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeSearchHistoryKeyGenerator Tests")
class RecipeSearchHistoryKeyGeneratorTest {

  private RecipeSearchHistoryKeyGenerator keyGenerator;

  @BeforeEach
  void setUp() {
    keyGenerator = new RecipeSearchHistoryKeyGenerator();
  }

  @Nested
  @DisplayName("generate 메서드 테스트")
  class GenerateTest {

    @Test
    @DisplayName("유효한 UUID로 키를 생성한다")
    void shouldGenerateKeyWithValidUuid() {
      UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

      String result = keyGenerator.generate(userId);

      assertThat(result).isEqualTo("recipeSearch:history:123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    @DisplayName("다른 UUID로 다른 키를 생성한다")
    void shouldGenerateDifferentKeyForDifferentUuid() {
      UUID userId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
      UUID userId2 = UUID.fromString("987fcdeb-51a2-43d1-b789-123456789abc");

      String key1 = keyGenerator.generate(userId1);
      String key2 = keyGenerator.generate(userId2);

      assertThat(key1).isNotEqualTo(key2);
      assertThat(key1).isEqualTo("recipeSearch:history:123e4567-e89b-12d3-a456-426614174000");
      assertThat(key2).isEqualTo("recipeSearch:history:987fcdeb-51a2-43d1-b789-123456789abc");
    }

    @Test
    @DisplayName("랜덤 UUID로 키를 생성한다")
    void shouldGenerateKeyWithRandomUuid() {
      UUID userId = UUID.randomUUID();

      String result = keyGenerator.generate(userId);

      assertThat(result).startsWith("recipeSearch:history:");
      assertThat(result).contains(userId.toString());
      assertThat(result).hasSize("recipeSearch:history:".length() + 36);
    }

    @Test
    @DisplayName("nil UUID로 키를 생성한다")
    void shouldGenerateKeyWithNilUuid() {
      UUID userId = new UUID(0L, 0L);

      String result = keyGenerator.generate(userId);

      assertThat(result).isEqualTo("recipeSearch:history:00000000-0000-0000-0000-000000000000");
    }

    @Test
    @DisplayName("동일한 UUID로 동일한 키를 생성한다")
    void shouldGenerateSameKeyForSameUuid() {
      UUID userId = UUID.randomUUID();

      String key1 = keyGenerator.generate(userId);
      String key2 = keyGenerator.generate(userId);

      assertThat(key1).isEqualTo(key2);
    }

    @Test
    @DisplayName("키 형식이 올바르다")
    void shouldGenerateKeyInCorrectFormat() {
      UUID userId = UUID.randomUUID();

      String result = keyGenerator.generate(userId);

      assertThat(result)
          .matches(
              "^recipeSearch:history:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("키 형식이 올바르다")
    void shouldHaveCorrectFormat() {
      UUID userId = UUID.randomUUID();

      String result = keyGenerator.generate(userId);

      assertThat(result)
          .matches(
              "^recipeSearch:history:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("키가 null이 아니다")
    void shouldNotReturnNull() {
      UUID userId = UUID.randomUUID();

      String result = keyGenerator.generate(userId);

      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("키가 빈 문자열이 아니다")
    void shouldNotReturnEmptyString() {
      UUID userId = UUID.randomUUID();

      String result = keyGenerator.generate(userId);

      assertThat(result).isNotEmpty();
    }
  }

  @Nested
  @DisplayName("키 생성 성능 테스트")
  class PerformanceTest {

    @Test
    @DisplayName("키 생성이 빠르게 실행된다")
    void shouldGenerateKeyQuickly() {
      UUID userId = UUID.randomUUID();

      long startTime = System.nanoTime();
      keyGenerator.generate(userId);
      long endTime = System.nanoTime();

      long executionTime = endTime - startTime;
      assertThat(executionTime).isLessThan(1_000_000L); // 1ms = 1,000,000 nanoseconds
    }

    @Test
    @DisplayName("여러 번 호출해도 일관된 성능을 보인다")
    void shouldMaintainConsistentPerformance() {
      UUID userId = UUID.randomUUID();
      int iterations = 1000;

      long totalTime = 0;
      for (int i = 0; i < iterations; i++) {
        long startTime = System.nanoTime();
        keyGenerator.generate(userId);
        long endTime = System.nanoTime();
        totalTime += (endTime - startTime);
      }

      double averageTime = (double) totalTime / iterations;
      assertThat(averageTime).isLessThan(1_000_000.0); // 1ms = 1,000,000 nanoseconds
    }
  }

  @Nested
  @DisplayName("키 생성 일관성 테스트")
  class ConsistencyTest {

    @Test
    @DisplayName("동일한 입력에 대해 항상 동일한 출력을 생성한다")
    void shouldAlwaysGenerateSameOutputForSameInput() {
      UUID userId = UUID.fromString("11111111-2222-3333-4444-555555555555");

      String key1 = keyGenerator.generate(userId);
      String key2 = keyGenerator.generate(userId);
      String key3 = keyGenerator.generate(userId);

      assertThat(key1).isEqualTo(key2);
      assertThat(key2).isEqualTo(key3);
      assertThat(key1).isEqualTo(key3);
    }

    @Test
    @DisplayName("다른 입력에 대해 다른 출력을 생성한다")
    void shouldGenerateDifferentOutputForDifferentInput() {
      UUID userId1 = UUID.fromString("11111111-2222-3333-4444-555555555555");
      UUID userId2 = UUID.fromString("66666666-7777-8888-9999-aaaaaaaaaaaa");

      String key1 = keyGenerator.generate(userId1);
      String key2 = keyGenerator.generate(userId2);

      assertThat(key1).isNotEqualTo(key2);
    }
  }
}
