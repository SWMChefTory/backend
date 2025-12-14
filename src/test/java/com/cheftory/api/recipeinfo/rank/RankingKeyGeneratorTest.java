package com.cheftory.api.recipeinfo.rank;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RankingKeyGenerator Tests")
class RankingKeyGeneratorTest {

  private final RankingKeyGenerator rankingKeyGenerator = new RankingKeyGenerator();

  private static final String MARKET_PREFIX = "korea:";
  private static final String TRENDING_RANKING_PREFIX = MARKET_PREFIX + "trendRecipe:ranking:";
  private static final String CHEF_RANKING_PREFIX = MARKET_PREFIX + "chefRecipe:ranking:";

  @Nested
  @DisplayName("키 생성")
  class GenerateKey {

    @Test
    @DisplayName("TRENDING 랭킹 키를 생성해야 한다")
    void shouldGenerateTrendingKey() {
      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        String result = rankingKeyGenerator.generateKey(RankingType.TRENDING);

        assertThat(result).startsWith(TRENDING_RANKING_PREFIX);
        assertThat(result).matches("korea:trendRecipe:ranking:\\d{14}");
      }
    }

    @Test
    @DisplayName("CHEF 랭킹 키를 생성해야 한다")
    void shouldGenerateChefKey() {
      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        String result = rankingKeyGenerator.generateKey(RankingType.CHEF);

        assertThat(result).startsWith(CHEF_RANKING_PREFIX);
        assertThat(result).matches("korea:chefRecipe:ranking:\\d{14}");
      }
    }
  }

  @Nested
  @DisplayName("최신 키 조회")
  class GetLatestKey {

    @Test
    @DisplayName("TRENDING 최신 키를 반환해야 한다")
    void shouldReturnTrendingLatestKey() {
      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        String result = rankingKeyGenerator.getLatestKey(RankingType.TRENDING);
        assertThat(result).isEqualTo("korea:trendRecipe:latest");
      }
    }

    @Test
    @DisplayName("CHEF 최신 키를 반환해야 한다")
    void shouldReturnChefLatestKey() {
      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        String result = rankingKeyGenerator.getLatestKey(RankingType.CHEF);
        assertThat(result).isEqualTo("korea:chefRecipe:latest");
      }
    }
  }

  @Nested
  @DisplayName("키 형식 검증")
  class KeyFormatValidation {

    @Test
    @DisplayName("생성된 키가 올바른 형식을 가져야 한다")
    void generatedKeyShouldHaveCorrectFormat() {
      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        String trendingKey = rankingKeyGenerator.generateKey(RankingType.TRENDING);
        String chefKey = rankingKeyGenerator.generateKey(RankingType.CHEF);

        assertThat(trendingKey).matches("korea:trendRecipe:ranking:\\d{14}");
        assertThat(chefKey).matches("korea:chefRecipe:ranking:\\d{14}");
      }
    }

    @Test
    @DisplayName("타임스탬프가 현재 시간(시 단위)과 일치해야 한다")
    void timestampShouldMatchCurrentHour() {
      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        String currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

        String key = rankingKeyGenerator.generateKey(RankingType.TRENDING);
        String timestamp = key.substring(TRENDING_RANKING_PREFIX.length());

        assertThat(timestamp).startsWith(currentHour);
        assertThat(timestamp).matches("\\d{14}");
      }
    }
  }
}