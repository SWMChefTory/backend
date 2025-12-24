package com.cheftory.api.recipe.rank;

import com.cheftory.api.recipe.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeRankService {
  private final RecipeRankRepository recipeRankRepository;
  private final RankingKeyGenerator rankingKeyGenerator;

  private static final Integer PAGE_SIZE = 10;
  private static final Duration TTL = Duration.ofDays(2);

  public void updateRecipes(RankingType type, List<UUID> recipeIds) {
    String newKey = rankingKeyGenerator.generateKey(type);

    IntStream.range(0, recipeIds.size())
        .boxed()
        .forEach(i -> recipeRankRepository.saveRanking(newKey, recipeIds.get(i), i + 1));

    recipeRankRepository.setExpire(newKey, TTL);
    recipeRankRepository.saveLatest(rankingKeyGenerator.getLatestKey(type), newKey);
  }

  public Page<UUID> getRecipeIds(RankingType type, Integer page) {
    Pageable pageable = PageRequest.of(page, 10);
    String latestPointerKey = rankingKeyGenerator.getLatestKey(type);

    String actualRankingKey =
        recipeRankRepository
            .findLatest(latestPointerKey)
            .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND));

    long offset = pageable.getOffset();
    long limitEnd = offset + pageable.getPageSize() - 1;

    Set<String> ids = recipeRankRepository.findRecipeIds(actualRankingKey, offset, limitEnd);

    List<UUID> recipeIds =
        (ids == null || ids.isEmpty()) ? List.of() : ids.stream().map(UUID::fromString).toList();

    String latestKey =
        recipeRankRepository
            .findLatest(rankingKeyGenerator.getLatestKey(type))
            .orElseThrow(() -> new RecipeRankException(RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND));
    Long totalElements = recipeRankRepository.count(latestKey);

    return new PageImpl<>(recipeIds, pageable, totalElements);
  }
}
