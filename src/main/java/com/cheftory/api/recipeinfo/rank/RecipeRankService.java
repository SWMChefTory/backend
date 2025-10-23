package com.cheftory.api.recipeinfo.rank;

import com.cheftory.api.recipeinfo.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipeinfo.rank.exception.RecipeRankException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeRankService {
  private final RecipeRankRepository recipeRankRepository;
  private final RankingKeyGenerator rankingKeyGenerator;

  private static final Integer PAGE_SIZE = 10;
  private static final Long EXPIRY_SECONDS = 172800L;

  public void updateRecipes(RankingType type, List<UUID> recipeIds) {
    String newKey = rankingKeyGenerator.generateKey(type);

    IntStream.range(0, recipeIds.size())
        .boxed()
        .forEach(i -> recipeRankRepository.saveRanking(newKey, recipeIds.get(i), i + 1));

    recipeRankRepository.setExpire(newKey, EXPIRY_SECONDS);
    recipeRankRepository.saveLatest(rankingKeyGenerator.getLatestKey(type), newKey);
  }

  public List<UUID> getRecipeIds(RankingType type, Integer page) {
    String latestKey = rankingKeyGenerator.getLatestKey(type);

    Long offset = page.longValue() * PAGE_SIZE;
    Long limitEnd = offset + PAGE_SIZE - 1;

    Set<String> ids = recipeRankRepository.findRecipeIds(latestKey, offset, limitEnd);

    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    return ids.stream().map(UUID::fromString).toList();
  }

  public Long getTotalCount(RankingType type) {
    String latestKey =
        recipeRankRepository
            .findLatest(rankingKeyGenerator.getLatestKey(type))
            .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND));
    return recipeRankRepository.count(latestKey);
  }
}
