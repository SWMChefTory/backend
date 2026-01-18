package com.cheftory.api.recipe.rank;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataRedisTest
@ActiveProfiles("test")
@DisplayName("RecipeRankRepository 통합 테스트")
class RecipeRankRepositoryTest {

  @Autowired private RedisTemplate<String, String> redisTemplate;

  private RecipeRankRepository recipeRankRepository;

  @BeforeEach
  void setUp() {
    recipeRankRepository = new RecipeRankRepository(redisTemplate);

    Assertions.assertNotNull(redisTemplate.getConnectionFactory());
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @Nested
  @DisplayName("saveRanking 메서드 테스트")
  class SaveRankingTest {

    @Test
    @DisplayName("레시피 랭킹을 저장할 수 있다")
    void shouldSaveRecipeRanking() {
      String key = "test:ranking";
      UUID recipeId = UUID.randomUUID();
      Integer rank = 1;

      recipeRankRepository.saveRanking(key, recipeId, rank);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }

    @Test
    @DisplayName("여러 레시피 랭킹을 저장할 수 있다")
    void shouldSaveMultipleRecipeRankings() {
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result)
          .containsExactlyInAnyOrder(
              recipeId1.toString(), recipeId2.toString(), recipeId3.toString());
    }

    @Test
    @DisplayName("동일한 레시피 ID로 랭킹을 업데이트할 수 있다")
    void shouldUpdateExistingRecipeRanking() {
      String key = "test:ranking";
      UUID recipeId = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.saveRanking(key, recipeId, 5);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result).containsExactly(recipeId.toString());
    }
  }

  @Nested
  @DisplayName("setExpire 메서드 테스트")
  class SetExpireTest {

    @Test
    @DisplayName("키에 만료 시간을 설정할 수 있다")
    void shouldSetExpireTime() throws InterruptedException {
      String key = "test:expire";
      UUID recipeId = UUID.randomUUID();
      Duration expireDuration = Duration.ofSeconds(1);

      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.setExpire(key, expireDuration);

      assertThat(recipeRankRepository.count(key)).isEqualTo(1);

      Thread.sleep(1100);

      assertThat(recipeRankRepository.count(key)).isEqualTo(0);
    }

    @Test
    @DisplayName("긴 만료 시간을 설정할 수 있다")
    void shouldSetLongExpireTime() {
      String key = "test:long:expire";
      UUID recipeId = UUID.randomUUID();
      Duration expireDuration = Duration.ofHours(1);

      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.setExpire(key, expireDuration);

      assertThat(recipeRankRepository.count(key)).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("saveLatest/findLatest 메서드 테스트")
  class SaveLatestFindLatestTest {

    @Test
    @DisplayName("최신 키 포인터를 저장하고 조회할 수 있다")
    void shouldSaveAndFindLatestKey() {
      String pointerKey = "test:latest:pointer";
      String realKey = "test:ranking:2024-01-01";

      recipeRankRepository.saveLatest(pointerKey, realKey);
      var result = recipeRankRepository.findLatest(pointerKey);

      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(realKey);
    }

    @Test
    @DisplayName("최신 키 포인터를 업데이트할 수 있다")
    void shouldUpdateLatestKey() {
      String pointerKey = "test:latest:pointer";
      String realKey1 = "test:ranking:2024-01-01";
      String realKey2 = "test:ranking:2024-01-02";

      recipeRankRepository.saveLatest(pointerKey, realKey1);
      recipeRankRepository.saveLatest(pointerKey, realKey2);
      var result = recipeRankRepository.findLatest(pointerKey);

      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(realKey2);
    }

    @Test
    @DisplayName("존재하지 않는 포인터 키를 조회하면 빈 Optional을 반환한다")
    void shouldReturnEmptyOptionalWhenPointerKeyNotFound() {
      String pointerKey = "test:nonexistent:pointer";

      var result = recipeRankRepository.findLatest(pointerKey);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 포인터 키를 조회하면 빈 Optional을 반환한다")
    void shouldReturnEmptyOptionalWhenPointerKeyIsBlank() {
      String pointerKey = "test:blank:pointer";
      String realKey = "";

      recipeRankRepository.saveLatest(pointerKey, realKey);
      var result = recipeRankRepository.findLatest(pointerKey);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findRecipeIds 메서드 테스트")
  class FindRecipeIdsTest {

    @Test
    @DisplayName("전체 레시피 ID 목록을 조회할 수 있다")
    void shouldFindAllRecipeIds() {
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);

      assertThat(result)
          .containsExactlyInAnyOrder(
              recipeId1.toString(), recipeId2.toString(), recipeId3.toString());
    }

    @Test
    @DisplayName("범위로 레시피 ID 목록을 조회할 수 있다")
    void shouldFindRecipeIdsByRange() {
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, 1);

      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 키로 조회하면 빈 결과를 반환한다")
    void shouldReturnEmptySetForNonExistentKey() {
      String key = "test:nonexistent";

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상위 N개 레시피 ID를 조회할 수 있다")
    void shouldFindTopNRecipeIds() {
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();
      UUID recipeId4 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);
      recipeRankRepository.saveRanking(key, recipeId4, 4);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, 2);

      assertThat(result).hasSize(3);
    }
  }

  @Nested
  @DisplayName("findRecipeIdsByRank 메서드 테스트")
  class FindRecipeIdsByRankTest {

    @Test
    @DisplayName("시작 랭크와 개수로 레시피 ID를 조회할 수 있다")
    void shouldFindRecipeIdsByRank() {
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();
      UUID recipeId4 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);
      recipeRankRepository.saveRanking(key, recipeId4, 4);

      var result = recipeRankRepository.findRecipeIdsByRank(key, 2, 2);

      assertThat(result).containsExactly(recipeId2.toString(), recipeId3.toString());
    }

    @Test
    @DisplayName("시작 랭크가 1 미만이면 첫 번째부터 조회한다")
    void shouldClampStartRankWhenLessThanOne() {
      String key = "test:ranking:clamp";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      var result = recipeRankRepository.findRecipeIdsByRank(key, 0, 2);

      assertThat(result).containsExactly(recipeId1.toString(), recipeId2.toString());
    }

    @Test
    @DisplayName("범위를 벗어난 랭크 조회는 빈 결과를 반환한다")
    void shouldReturnEmptyWhenStartRankOutOfBounds() {
      String key = "test:ranking:out-of-bounds";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);

      var result = recipeRankRepository.findRecipeIdsByRank(key, 10, 3);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("count 메서드 테스트")
  class CountTest {

    @Test
    @DisplayName("저장된 레시피 개수를 조회할 수 있다")
    void shouldCountRecipeIds() {
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      Long count = recipeRankRepository.count(key);

      assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하지 않는 키의 개수는 0을 반환한다")
    void shouldReturnZeroForNonExistentKey() {
      String key = "test:nonexistent";

      Long count = recipeRankRepository.count(key);

      assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("빈 키의 개수는 0을 반환한다")
    void shouldReturnZeroForEmptyKey() {
      String key = "test:empty";

      Long count = recipeRankRepository.count(key);

      assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("중복 저장 후 개수를 조회하면 1을 반환한다")
    void shouldReturnOneForDuplicateKeys() {
      String key = "test:duplicate";
      UUID recipeId = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.saveRanking(key, recipeId, 2);

      Long count = recipeRankRepository.count(key);

      assertThat(count).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class BoundaryValueTest {

    @Test
    @DisplayName("매우 큰 랭킹 값을 저장할 수 있다")
    void shouldSaveVeryLargeRankingValue() {
      String key = "test:large:ranking";
      UUID recipeId = UUID.randomUUID();
      Integer largeRank = Integer.MAX_VALUE;

      recipeRankRepository.saveRanking(key, recipeId, largeRank);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }

    @Test
    @DisplayName("랭킹 값 0을 저장할 수 있다")
    void shouldSaveZeroRankingValue() {
      String key = "test:zero:ranking";
      UUID recipeId = UUID.randomUUID();
      Integer zeroRank = 0;

      recipeRankRepository.saveRanking(key, recipeId, zeroRank);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }

    @Test
    @DisplayName("음수 랭킹 값을 저장할 수 있다")
    void shouldSaveNegativeRankingValue() {
      String key = "test:negative:ranking";
      UUID recipeId = UUID.randomUUID();
      Integer negativeRank = -1;

      recipeRankRepository.saveRanking(key, recipeId, negativeRank);

      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }

    @Test
    @DisplayName("매우 긴 키 이름을 처리할 수 있다")
    void shouldHandleVeryLongKeyName() {
      String longKey = "test:very:long:key:name:with:many:segments:" + "a".repeat(1000);
      UUID recipeId = UUID.randomUUID();
      Integer rank = 1;

      recipeRankRepository.saveRanking(longKey, recipeId, rank);

      Set<String> result = recipeRankRepository.findRecipeIds(longKey, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }

    @Test
    @DisplayName("빈 키 이름을 처리할 수 있다")
    void shouldHandleEmptyKeyName() {
      String emptyKey = "";
      UUID recipeId = UUID.randomUUID();
      Integer rank = 1;

      recipeRankRepository.saveRanking(emptyKey, recipeId, rank);

      Set<String> result = recipeRankRepository.findRecipeIds(emptyKey, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }
  }

  @Nested
  @DisplayName("동시성 테스트")
  class ConcurrencyTest {

    @Test
    @DisplayName("동시에 여러 랭킹을 저장할 수 있다")
    void shouldSaveMultipleRankingsConcurrently() throws InterruptedException {
      String key = "test:concurrent:ranking";
      int threadCount = 10;
      int recipesPerThread = 5;
      Thread[] threads = new Thread[threadCount];

      for (int i = 0; i < threadCount; i++) {
        final int threadIndex = i;
        threads[i] =
            new Thread(
                () -> {
                  for (int j = 0; j < recipesPerThread; j++) {
                    UUID recipeId = UUID.randomUUID();
                    Integer rank = threadIndex * recipesPerThread + j + 1;
                    recipeRankRepository.saveRanking(key, recipeId, rank);
                  }
                });
        threads[i].start();
      }

      for (Thread thread : threads) {
        thread.join();
      }

      Long count = recipeRankRepository.count(key);
      assertThat(count).isEqualTo(threadCount * recipesPerThread);
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTest {

    @Test
    @DisplayName("전체 랭킹 시스템 시나리오를 테스트할 수 있다")
    void shouldHandleCompleteRankingScenario() {
      String pointerKey = "test:latest:pointer";
      String rankingKey = "test:ranking:2024-01-01";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(rankingKey, recipeId1, 1);
      recipeRankRepository.saveRanking(rankingKey, recipeId2, 2);
      recipeRankRepository.saveRanking(rankingKey, recipeId3, 3);

      recipeRankRepository.saveLatest(pointerKey, rankingKey);

      recipeRankRepository.setExpire(rankingKey, Duration.ofSeconds(5));

      var latestKey = recipeRankRepository.findLatest(pointerKey);
      assertThat(latestKey).isPresent();
      assertThat(latestKey.get()).isEqualTo(rankingKey);

      Long count = recipeRankRepository.count(rankingKey);
      assertThat(count).isEqualTo(3);

      Set<String> top2 = recipeRankRepository.findRecipeIds(rankingKey, 0, 1);
      assertThat(top2).hasSize(2);

      Set<String> allRecipes = recipeRankRepository.findRecipeIds(rankingKey, 0, -1);
      assertThat(allRecipes)
          .containsExactlyInAnyOrder(
              recipeId1.toString(), recipeId2.toString(), recipeId3.toString());
    }

    @Test
    @DisplayName("랭킹 업데이트 시나리오를 테스트할 수 있다")
    void shouldHandleRankingUpdateScenario() {
      String rankingKey = "test:ranking:update";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(rankingKey, recipeId1, 1);
      recipeRankRepository.saveRanking(rankingKey, recipeId2, 2);
      recipeRankRepository.saveRanking(rankingKey, recipeId3, 3);

      recipeRankRepository.saveRanking(rankingKey, recipeId2, 1);
      recipeRankRepository.saveRanking(rankingKey, recipeId1, 2);

      Long count = recipeRankRepository.count(rankingKey);
      assertThat(count).isEqualTo(3);

      Set<String> allRecipes = recipeRankRepository.findRecipeIds(rankingKey, 0, -1);
      assertThat(allRecipes)
          .containsExactlyInAnyOrder(
              recipeId1.toString(), recipeId2.toString(), recipeId3.toString());
    }
  }
}
