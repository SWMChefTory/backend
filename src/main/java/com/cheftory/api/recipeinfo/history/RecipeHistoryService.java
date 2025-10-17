package com.cheftory.api.recipeinfo.history;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.history.exception.ViewStatusErrorCode;
import com.cheftory.api.recipeinfo.history.exception.ViewStatusException;
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
// 사용자의 시청 상태,
public class RecipeHistoryService {
  private final RecipeHistoryRepository recipeHistoryRepository;
  private final Clock clock;

  @Transactional
  public void create(UUID userId, UUID recipeId) {
    if (!recipeHistoryRepository.existsByRecipeIdAndUserIdAndStatus(
        recipeId, userId, RecipeViewState.ACTIVE)) {
      recipeHistoryRepository.save(RecipeHistory.create(clock, userId, recipeId));
    }
  }

  @Transactional
  public RecipeHistory get(UUID userId, UUID recipeId) {
    RecipeHistory recipeHistory =
        recipeHistoryRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
            .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeHistory.updateViewedAt(clock);
    return recipeHistoryRepository.save(recipeHistory);
  }

  @Transactional
  public void updateCategory(UUID userId, UUID recipeId, UUID categoryId) {
    RecipeHistory recipeHistory =
        recipeHistoryRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
            .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeHistory.updateRecipeCategoryId(categoryId);
    recipeHistoryRepository.save(recipeHistory);
  }

  @Transactional
  public void deleteCategories(UUID categoryId) {
    List<RecipeHistory> viewStatuses =
        recipeHistoryRepository.findByRecipeCategoryIdAndStatus(categoryId, RecipeViewState.ACTIVE);
    viewStatuses.forEach(RecipeHistory::emptyRecipeCategoryId);
    recipeHistoryRepository.saveAll(viewStatuses);
  }

  @Transactional
  public void delete(UUID userId, UUID recipeId) {
    RecipeHistory recipeHistory =
        recipeHistoryRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
            .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeHistory.delete();
    recipeHistoryRepository.save(recipeHistory);
  }

  public Page<RecipeHistory> getCategories(UUID userId, UUID categoryId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, categoryId, RecipeViewState.ACTIVE, pageable);
  }

  public Page<RecipeHistory> getUnCategories(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, null, RecipeViewState.ACTIVE, pageable);
  }

  public Page<RecipeHistory> getAll(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndStatus(
        userId, RecipeViewState.ACTIVE, pageable);
  }

  public Page<RecipeHistory> getRecentUsers(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, pageable);
  }

  public List<RecipeHistoryCount> countByCategories(List<UUID> categoryIds) {
    List<RecipeHistoryCountProjection> projections =
        recipeHistoryRepository.countByCategoryIdsAndStatus(categoryIds, RecipeViewState.ACTIVE);
    return projections.stream()
        .map(
            projection ->
                RecipeHistoryCount.of(projection.getCategoryId(), projection.getCount().intValue()))
        .toList();
  }

  public List<RecipeHistory> getUsers(List<UUID> recipeIds, UUID userId) {
    return recipeHistoryRepository.findByRecipeIdInAndUserIdAndStatus(
        recipeIds, userId, RecipeViewState.ACTIVE);
  }
}
