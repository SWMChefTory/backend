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
    if (recipeViewStatusRepository.existsByRecipeIdAndUserId(recipeId, userId)) {
      throw new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_ALREADY_EXISTS);
    }
    recipeViewStatusRepository.save(RecipeViewStatus.create(clock, userId, recipeId));
  }

  @Transactional
  public RecipeViewStatus find(UUID userId, UUID recipeId) {
    RecipeViewStatus recipeViewStatus = recipeViewStatusRepository.findByRecipeIdAndUserId(recipeId, userId)
        .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateViewedAt(clock);
    return recipeViewStatusRepository.save(recipeViewStatus);
  }

  @Transactional
  public void updateCategory(UUID userId, UUID recipeId, UUID categoryId) {
    RecipeViewStatus recipeViewStatus = recipeViewStatusRepository.findByRecipeIdAndUserId(recipeId, userId)
        .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateRecipeCategoryId(categoryId);
    recipeViewStatusRepository.save(recipeViewStatus);
  }

  @Transactional
  public void deleteCategories(UUID categoryId) {
    List<RecipeViewStatus> viewStatuses = recipeViewStatusRepository.findByRecipeCategoryId(categoryId);
    viewStatuses.forEach(RecipeViewStatus::emptyRecipeCategoryId);
    recipeViewStatusRepository.saveAll(viewStatuses);
  }

  public List<RecipeViewStatus> findCategories(UUID userId, UUID categoryId) {
    return recipeViewStatusRepository.findAllByUserIdAndRecipeCategoryId(userId, categoryId);
  }

  public List<RecipeViewStatus> findUnCategories(UUID userId) {
    return recipeViewStatusRepository.findAllByUserIdAndRecipeCategoryId(userId, null);
  }

  public List<RecipeViewStatus> findRecentUsers(UUID userId) {
    return recipeViewStatusRepository.findByUserId(userId, ViewStatusSort.VIEWED_AT_DESC);
  }

  public List<RecipeViewStatusCount> countByCategories(List<UUID> categoryIds) {
    List<RecipeViewStatusCountProjection> projections = recipeViewStatusRepository.countByCategoryIds(categoryIds);
    return projections.stream()
        .map(projection -> RecipeViewStatusCount.of(projection.getCategoryId(), projection.getCount().intValue()))
        .toList();
  }
}
