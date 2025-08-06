package com.cheftory.api.recipe.viewstatus;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.viewstatus.exception.ViewStatusErrorCode;
import com.cheftory.api.recipe.viewstatus.exception.ViewStatusException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeViewStatusService {
  private final RecipeViewStatusRepository recipeViewStatusRepository;
  private final Clock clock;

  @Transactional
  public void create(UUID userId, UUID recipeId) {
    if (!recipeViewStatusRepository.existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)) {
      recipeViewStatusRepository.save(RecipeViewStatus.create(clock, userId, recipeId));
    }
  }

  @Transactional
  public RecipeViewStatus find(UUID userId, UUID recipeId) {
    RecipeViewStatus recipeViewStatus = recipeViewStatusRepository.findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
        .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateViewedAt(clock);
    return recipeViewStatusRepository.save(recipeViewStatus);
  }

  @Transactional
  public void updateCategory(UUID userId, UUID recipeId, UUID categoryId) {
    RecipeViewStatus recipeViewStatus = recipeViewStatusRepository.findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
        .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateRecipeCategoryId(categoryId);
    recipeViewStatusRepository.save(recipeViewStatus);
  }

  @Transactional
  public void deleteCategories(UUID categoryId) {
    List<RecipeViewStatus> viewStatuses = recipeViewStatusRepository.findByRecipeCategoryIdAndStatus(categoryId, RecipeViewState.ACTIVE);
    viewStatuses.forEach(RecipeViewStatus::emptyRecipeCategoryId);
    recipeViewStatusRepository.saveAll(viewStatuses);
  }

  @Transactional
  public void delete(UUID userId, UUID recipeId) {
    RecipeViewStatus recipeViewStatus = recipeViewStatusRepository.findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
        .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.delete();
    recipeViewStatusRepository.save(recipeViewStatus);
  }

  public List<RecipeViewStatus> findCategories(UUID userId, UUID categoryId) {
    return recipeViewStatusRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(userId, categoryId, RecipeViewState.ACTIVE);
  }

  public List<RecipeViewStatus> findUnCategories(UUID userId) {
    return recipeViewStatusRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(userId, null, RecipeViewState.ACTIVE);
  }

  public List<RecipeViewStatus> findRecentUsers(UUID userId) {
    return recipeViewStatusRepository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, ViewStatusSort.VIEWED_AT_DESC);
  }

  public List<RecipeViewStatusCount> countByCategories(List<UUID> categoryIds) {
    List<RecipeViewStatusCountProjection> projections = recipeViewStatusRepository.countByCategoryIdsAndStatus(categoryIds, RecipeViewState.ACTIVE);
    return projections.stream()
        .map(projection -> RecipeViewStatusCount.of(projection.getCategoryId(), projection.getCount().intValue()))
        .toList();
  }
}
