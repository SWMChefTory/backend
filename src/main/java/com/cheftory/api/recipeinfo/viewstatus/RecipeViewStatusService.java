package com.cheftory.api.recipeinfo.viewstatus;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import com.cheftory.api.recipeinfo.viewstatus.exception.ViewStatusErrorCode;
import com.cheftory.api.recipeinfo.viewstatus.exception.ViewStatusException;
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
public class RecipeViewStatusService {
  private final RecipeViewStatusRepository recipeViewStatusRepository;
  private final Clock clock;

  @Transactional
  public void create(UUID userId, UUID recipeId) {
    if (!recipeViewStatusRepository.existsByRecipeIdAndUserIdAndStatus(
        recipeId, userId, RecipeViewState.ACTIVE)) {
      recipeViewStatusRepository.save(RecipeViewStatus.create(clock, userId, recipeId));
    }
  }

  @Transactional
  public RecipeViewStatus get(UUID userId, UUID recipeId) {
    RecipeViewStatus recipeViewStatus =
        recipeViewStatusRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
            .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateViewedAt(clock);
    return recipeViewStatusRepository.save(recipeViewStatus);
  }

  @Transactional
  public void updateCategory(UUID userId, UUID recipeId, UUID categoryId) {
    RecipeViewStatus recipeViewStatus =
        recipeViewStatusRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
            .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateRecipeCategoryId(categoryId);
    recipeViewStatusRepository.save(recipeViewStatus);
  }

  @Transactional
  public void deleteCategories(UUID categoryId) {
    List<RecipeViewStatus> viewStatuses =
        recipeViewStatusRepository.findByRecipeCategoryIdAndStatus(
            categoryId, RecipeViewState.ACTIVE);
    viewStatuses.forEach(RecipeViewStatus::emptyRecipeCategoryId);
    recipeViewStatusRepository.saveAll(viewStatuses);
  }

  @Transactional
  public void delete(UUID userId, UUID recipeId) {
    RecipeViewStatus recipeViewStatus =
        recipeViewStatusRepository
            .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
            .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.delete();
    recipeViewStatusRepository.save(recipeViewStatus);
  }

  public Page<RecipeViewStatus> getCategories(UUID userId, UUID categoryId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, ViewStatusSort.VIEWED_AT_DESC);
    return recipeViewStatusRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, categoryId, RecipeViewState.ACTIVE, pageable);
  }

  public Page<RecipeViewStatus> getUnCategories(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, ViewStatusSort.VIEWED_AT_DESC);
    return recipeViewStatusRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, null, RecipeViewState.ACTIVE, pageable);
  }

  public Page<RecipeViewStatus> getRecentUsers(UUID userId, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, ViewStatusSort.VIEWED_AT_DESC);
    return recipeViewStatusRepository.findByUserIdAndStatus(
        userId, RecipeViewState.ACTIVE, pageable);
  }

  public List<RecipeViewStatusCount> countByCategories(List<UUID> categoryIds) {
    List<RecipeViewStatusCountProjection> projections =
        recipeViewStatusRepository.countByCategoryIdsAndStatus(categoryIds, RecipeViewState.ACTIVE);
    return projections.stream()
        .map(
            projection ->
                RecipeViewStatusCount.of(
                    projection.getCategoryId(), projection.getCount().intValue()))
        .toList();
  }
}
