package com.cheftory.api.recipeinfo.rank;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
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
    // Repository 인스턴스 생성
    recipeRankRepository = new RecipeRankRepository(redisTemplate);

    // 테스트 전 Redis 데이터 초기화
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @Nested
  @DisplayName("saveRanking 메서드 테스트")
  class SaveRankingTest {

    @Test
    @DisplayName("레시피 랭킹을 저장할 수 있다")
    void shouldSaveRecipeRanking() {
      // Given
      String key = "test:ranking";
      UUID recipeId = UUID.randomUUID();
      Integer rank = 1;

      // When
      recipeRankRepository.saveRanking(key, recipeId, rank);

      // Then
      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result).contains(recipeId.toString());
    }

    @Test
    @DisplayName("여러 레시피 랭킹을 저장할 수 있다")
    void shouldSaveMultipleRecipeRankings() {
      // Given
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      // When
      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      // Then
      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);
      assertThat(result)
          .containsExactlyInAnyOrder(
              recipeId1.toString(), recipeId2.toString(), recipeId3.toString());
    }

    @Test
    @DisplayName("동일한 레시피 ID로 랭킹을 업데이트할 수 있다")
    void shouldUpdateExistingRecipeRanking() {
      // Given
      String key = "test:ranking";
      UUID recipeId = UUID.randomUUID();

      // When
      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.saveRanking(key, recipeId, 5);

      // Then
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
      // Given
      String key = "test:expire";
      UUID recipeId = UUID.randomUUID();
      Long expireSeconds = 1L;

      // When
      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.setExpire(key, expireSeconds);

      // Then
      assertThat(recipeRankRepository.count(key)).isEqualTo(1);

      // 만료 시간 대기
      Thread.sleep(1100);

      // 만료 후 확인
      assertThat(recipeRankRepository.count(key)).isEqualTo(0);
    }

    @Test
    @DisplayName("긴 만료 시간을 설정할 수 있다")
    void shouldSetLongExpireTime() {
      // Given
      String key = "test:long:expire";
      UUID recipeId = UUID.randomUUID();
      Long expireSeconds = 3600L; // 1시간

      // When
      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.setExpire(key, expireSeconds);

      // Then
      assertThat(recipeRankRepository.count(key)).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("saveLatest/findLatest 메서드 테스트")
  class SaveLatestFindLatestTest {

    @Test
    @DisplayName("최신 키 포인터를 저장하고 조회할 수 있다")
    void shouldSaveAndFindLatestKey() {
      // Given
      String pointerKey = "test:latest:pointer";
      String realKey = "test:ranking:2024-01-01";

      // When
      recipeRankRepository.saveLatest(pointerKey, realKey);
      var result = recipeRankRepository.findLatest(pointerKey);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(realKey);
    }

    @Test
    @DisplayName("최신 키 포인터를 업데이트할 수 있다")
    void shouldUpdateLatestKey() {
      // Given
      String pointerKey = "test:latest:pointer";
      String realKey1 = "test:ranking:2024-01-01";
      String realKey2 = "test:ranking:2024-01-02";

      // When
      recipeRankRepository.saveLatest(pointerKey, realKey1);
      recipeRankRepository.saveLatest(pointerKey, realKey2);
      var result = recipeRankRepository.findLatest(pointerKey);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(realKey2);
    }

    @Test
    @DisplayName("존재하지 않는 포인터 키를 조회하면 빈 Optional을 반환한다")
    void shouldReturnEmptyOptionalWhenPointerKeyNotFound() {
      // Given
      String pointerKey = "test:nonexistent:pointer";

      // When
      var result = recipeRankRepository.findLatest(pointerKey);

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 포인터 키를 조회하면 빈 Optional을 반환한다")
    void shouldReturnEmptyOptionalWhenPointerKeyIsBlank() {
      // Given
      String pointerKey = "test:blank:pointer";
      String realKey = "";

      // When
      recipeRankRepository.saveLatest(pointerKey, realKey);
      var result = recipeRankRepository.findLatest(pointerKey);

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findRecipeIds 메서드 테스트")
  class FindRecipeIdsTest {

    @Test
    @DisplayName("전체 레시피 ID 목록을 조회할 수 있다")
    void shouldFindAllRecipeIds() {
      // Given
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      // When
      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);

      // Then
      assertThat(result)
          .containsExactlyInAnyOrder(
              recipeId1.toString(), recipeId2.toString(), recipeId3.toString());
    }

    @Test
    @DisplayName("범위로 레시피 ID 목록을 조회할 수 있다")
    void shouldFindRecipeIdsByRange() {
      // Given
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      // When
      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, 1);

      // Then
      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 키로 조회하면 빈 결과를 반환한다")
    void shouldReturnEmptySetForNonExistentKey() {
      // Given
      String key = "test:nonexistent";

      // When
      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, -1);

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상위 N개 레시피 ID를 조회할 수 있다")
    void shouldFindTopNRecipeIds() {
      // Given
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();
      UUID recipeId4 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);
      recipeRankRepository.saveRanking(key, recipeId4, 4);

      // When
      Set<String> result = recipeRankRepository.findRecipeIds(key, 0, 2);

      // Then
      assertThat(result).hasSize(3); // 0, 1, 2 인덱스
    }
  }

  @Nested
  @DisplayName("count 메서드 테스트")
  class CountTest {

    @Test
    @DisplayName("저장된 레시피 개수를 조회할 수 있다")
    void shouldCountRecipeIds() {
      // Given
      String key = "test:ranking";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId1, 1);
      recipeRankRepository.saveRanking(key, recipeId2, 2);
      recipeRankRepository.saveRanking(key, recipeId3, 3);

      // When
      Long count = recipeRankRepository.count(key);

      // Then
      assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하지 않는 키의 개수는 0을 반환한다")
    void shouldReturnZeroForNonExistentKey() {
      // Given
      String key = "test:nonexistent";

      // When
      Long count = recipeRankRepository.count(key);

      // Then
      assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("빈 키의 개수는 0을 반환한다")
    void shouldReturnZeroForEmptyKey() {
      // Given
      String key = "test:empty";

      // When
      Long count = recipeRankRepository.count(key);

      // Then
      assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("중복 저장 후 개수를 조회하면 1을 반환한다")
    void shouldReturnOneForDuplicateKeys() {
      // Given
      String key = "test:duplicate";
      UUID recipeId = UUID.randomUUID();

      recipeRankRepository.saveRanking(key, recipeId, 1);
      recipeRankRepository.saveRanking(key, recipeId, 2); // 중복 저장

      // When
      Long count = recipeRankRepository.count(key);

      // Then
      assertThat(count).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTest {

    @Test
    @DisplayName("전체 랭킹 시스템 시나리오를 테스트할 수 있다")
    void shouldHandleCompleteRankingScenario() {
      // Given
      String pointerKey = "test:latest:pointer";
      String rankingKey = "test:ranking:2024-01-01";
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID recipeId3 = UUID.randomUUID();

      // When - 랭킹 데이터 저장
      recipeRankRepository.saveRanking(rankingKey, recipeId1, 1);
      recipeRankRepository.saveRanking(rankingKey, recipeId2, 2);
      recipeRankRepository.saveRanking(rankingKey, recipeId3, 3);

      // 최신 키 포인터 설정
      recipeRankRepository.saveLatest(pointerKey, rankingKey);

      // 만료 시간 설정 (테스트를 위해 짧게)
      recipeRankRepository.setExpire(rankingKey, 5L);

      // Then - 데이터 검증
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
  }
}
