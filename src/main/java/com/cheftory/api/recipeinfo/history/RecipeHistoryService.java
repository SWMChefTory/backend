package com.cheftory.api.recipeinfo.history;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.history.exception.RecipeHistoryErrorCode;
import com.cheftory.api.recipeinfo.history.exception.RecipeHistoryException;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeHistoryService {
  private final RecipeHistoryRepository recipeHistoryRepository;
  private final Clock clock;

  @Transactional
  public void create(UUID userId, UUID recipeId) {
    if (!recipeHistoryRepository.existsByRecipeIdAndUserIdAndStatus(
        recipeId, userId, RecipeHistoryStatus.ACTIVE)) {
      recipeHistoryRepository.save(RecipeHistory.create(clock, userId, recipeId));
    }
  }

  @Transactional
  public RecipeHistory get(UUID userId, UUID recipeId) {
    RecipeHistory recipeHistory =
        recipeHistoryRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE)
            .orElseThrow(
                () -> new RecipeHistoryException(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND));
    recipeHistory.updateViewedAt(clock);
    return recipeHistoryRepository.save(recipeHistory);
  }

  @Transactional
  public void updateCategory(UUID userId, UUID recipeId, UUID categoryId) {
    RecipeHistory recipeHistory =
        recipeHistoryRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE)
            .orElseThrow(
                () -> new RecipeHistoryException(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND));
    recipeHistory.updateRecipeCategoryId(categoryId);
    recipeHistoryRepository.save(recipeHistory);
  }

  @Transactional
  public void unCategorize(UUID categoryId) {
    List<RecipeHistory> viewStatuses =
        recipeHistoryRepository.findByRecipeCategoryIdAndStatus(
            categoryId, RecipeHistoryStatus.ACTIVE);
    viewStatuses.forEach(RecipeHistory::emptyRecipeCategoryId);
    recipeHistoryRepository.saveAll(viewStatuses);
  }

  @Transactional
  public void delete(UUID userId, UUID recipeId) {
    RecipeHistory recipeHistory =
        recipeHistoryRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE)
            .orElseThrow(
                () -> new RecipeHistoryException(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND));
    recipeHistory.delete();
    recipeHistoryRepository.save(recipeHistory);
  }

  @Transactional
  public void deleteByRecipe(UUID recipeId) {
    List<RecipeHistory> histories = recipeHistoryRepository.findAllByRecipeId(recipeId);
    histories.forEach(RecipeHistory::delete);
    recipeHistoryRepository.saveAll(histories);
  }

  @Transactional
  public void blockByRecipe(UUID recipeId) {
    List<RecipeHistory> histories = recipeHistoryRepository.findAllByRecipeId(recipeId);
    histories.forEach(RecipeHistory::block);
    recipeHistoryRepository.saveAll(histories);
  }

  public Page<RecipeHistory> getCategorized(UUID userId, UUID categoryId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, categoryId, RecipeHistoryStatus.ACTIVE, pageable);
  }

  public Page<RecipeHistory> getUnCategorized(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, null, RecipeHistoryStatus.ACTIVE, pageable);
  }

  public Page<RecipeHistory> getRecents(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findByUserIdAndStatus(
        userId, RecipeHistoryStatus.ACTIVE, pageable);
  }

  public List<RecipeHistoryCategorizedCount> countByCategories(List<UUID> categoryIds) {
    List<RecipeHistoryCategorizedCountProjection> projections =
        recipeHistoryRepository.countByCategoryIdsAndStatus(
            categoryIds, RecipeHistoryStatus.ACTIVE);
    return projections.stream()
        .map(
            projection ->
                RecipeHistoryCategorizedCount.of(
                    projection.getCategoryId(), projection.getCount().intValue()))
        .toList();
  }

  public RecipeHistoryUnCategorizedCount countUncategorized(UUID userId) {
    RecipeHistoryUnCategorizedCountProjection projection =
        recipeHistoryRepository.countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);
    return RecipeHistoryUnCategorizedCount.of(projection.getCount().intValue());
  }

  public List<RecipeHistory> getByRecipes(List<UUID> recipeIds, UUID userId) {
    return recipeHistoryRepository.findByRecipeIdInAndUserIdAndStatus(
        recipeIds, userId, RecipeHistoryStatus.ACTIVE);
  }
}
