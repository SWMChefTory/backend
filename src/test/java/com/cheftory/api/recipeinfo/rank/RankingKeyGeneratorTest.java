package com.cheftory.api.recipeinfo.rank;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RankingKeyGenerator Tests")
public class RankingKeyGeneratorTest {

  private RankingKeyGenerator rankingKeyGenerator;

  @BeforeEach
  void setUp() {
    rankingKeyGenerator = new RankingKeyGenerator();
  }

  @Nested
  @DisplayName("키 생성")
  class GenerateKey {

    @Nested
    @DisplayName("Given - TRENDING 랭킹 타입이 주어졌을 때")
    class GivenTrendingRankingType {

      @Test
      @DisplayName("Then - 트렌드 레시피 랭킹 키를 생성해야 한다")
      void thenShouldGenerateTrendingKey() {
        String result = rankingKeyGenerator.generateKey(RankingType.TRENDING);

        assertThat(result).startsWith("trendRecipe:ranking:");
        assertThat(result).matches("trendRecipe:ranking:\\d{14}");
      }
    }

    @Nested
    @DisplayName("Given - CHEF 랭킹 타입이 주어졌을 때")
    class GivenChefRankingType {

      @Test
      @DisplayName("Then - 셰프 레시피 랭킹 키를 생성해야 한다")
      void thenShouldGenerateChefKey() {
        String result = rankingKeyGenerator.generateKey(RankingType.CHEF);

        assertThat(result).startsWith("chefRecipe:ranking:");
        assertThat(result).matches("chefRecipe:ranking:\\d{14}");
      }
    }

    @Nested
    @DisplayName("Given - 같은 시간에 여러 번 호출될 때")
    class GivenMultipleCallsAtSameTime {

      @Test
      @DisplayName("Then - 같은 타임스탬프를 포함한 키를 생성해야 한다")
      void thenShouldGenerateKeysWithSameTimestamp() {
        String key1 = rankingKeyGenerator.generateKey(RankingType.TRENDING);
        String key2 = rankingKeyGenerator.generateKey(RankingType.TRENDING);

        // 시간 부분만 추출
        String timestamp1 = key1.substring("trendRecipe:ranking:".length());
        String timestamp2 = key2.substring("trendRecipe:ranking:".length());

        // 같은 시간에 생성된 키는 같은 타임스탬프를 가져야 함
        assertThat(timestamp1).isEqualTo(timestamp2);
      }
    }
  }

  @Nested
  @DisplayName("최신 키 조회")
  class GetLatestKey {

    @Nested
    @DisplayName("Given - TRENDING 랭킹 타입이 주어졌을 때")
    class GivenTrendingRankingType {

      @Test
      @DisplayName("Then - 트렌드 레시피 최신 키를 반환해야 한다")
      void thenShouldReturnTrendingLatestKey() {
        String result = rankingKeyGenerator.getLatestKey(RankingType.TRENDING);

        assertThat(result).isEqualTo("trendRecipe:latest");
      }
    }

    @Nested
    @DisplayName("Given - CHEF 랭킹 타입이 주어졌을 때")
    class GivenChefRankingType {

      @Test
      @DisplayName("Then - 셰프 레시피 최신 키를 반환해야 한다")
      void thenShouldReturnChefLatestKey() {
        String result = rankingKeyGenerator.getLatestKey(RankingType.CHEF);

        assertThat(result).isEqualTo("chefRecipe:latest");
      }
    }
  }

  @Nested
  @DisplayName("키 형식 검증")
  class KeyFormatValidation {

    @Test
    @DisplayName("생성된 키가 올바른 형식을 가져야 한다")
    void generatedKeyShouldHaveCorrectFormat() {
      String trendingKey = rankingKeyGenerator.generateKey(RankingType.TRENDING);
      String chefKey = rankingKeyGenerator.generateKey(RankingType.CHEF);

      // 트렌드 키 형식 검증
      assertThat(trendingKey).matches("trendRecipe:ranking:\\d{14}");

      // 셰프 키 형식 검증
      assertThat(chefKey).matches("chefRecipe:ranking:\\d{14}");
    }

    @Test
    @DisplayName("타임스탬프가 현재 시간과 일치해야 한다")
    void timestampShouldMatchCurrentTime() {
      String key = rankingKeyGenerator.generateKey(RankingType.TRENDING);
      String timestamp = key.substring("trendRecipe:ranking:".length());

      // 현재 시간과 비교 (초 단위까지)
      String currentTime =
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
      String keyTime = timestamp;

      // 같은 분 내에 생성되었다면 같은 값을 가져야 함
      assertThat(keyTime).startsWith(currentTime.substring(0, 12)); // 분 단위까지 비교
    }
  }
}
