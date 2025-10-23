package com.cheftory.api.recipeinfo.rank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class RankingKeyGenerator {
  public String generateKey(RankingType type) {
    String prefix =
        switch (type) {
          case TRENDING -> "trendRecipe:ranking:";
          case CHEF -> "chefRecipe:ranking:";
        };
    return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
  }

  public String getLatestKey(RankingType type) {
    return switch (type) {
      case TRENDING -> "trendRecipe:latest";
      case CHEF -> "chefRecipe:latest";
    };
  }
}
