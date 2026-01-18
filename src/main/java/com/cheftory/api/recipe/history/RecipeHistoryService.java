package com.cheftory.api.recipe.history;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPageable;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.ViewedAtCursor;
import com.cheftory.api._common.cursor.ViewedAtCursorCodec;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCount;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCountProjection;
import com.cheftory.api.recipe.history.entity.RecipeHistoryStatus;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCount;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCountProjection;
import com.cheftory.api.recipe.history.exception.RecipeHistoryErrorCode;
import com.cheftory.api.recipe.history.exception.RecipeHistoryException;
import com.cheftory.api.recipe.history.utils.RecipeHistorySort;
import com.cheftory.api.recipe.util.RecipePageRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeHistoryService {
  private final RecipeHistoryRepository recipeHistoryRepository;
  private final ViewedAtCursorCodec viewedAtCursorCodec;
  private final Clock clock;

  @Transactional
  public boolean create(UUID userId, UUID recipeId) {
    try {
      recipeHistoryRepository.save(RecipeHistory.create(clock, userId, recipeId));
      return true;
    } catch (DataIntegrityViolationException e) {
      var existingOpt = recipeHistoryRepository.findByUserIdAndRecipeId(userId, recipeId);

      if (existingOpt.isPresent()) {
        RecipeHistory existing = existingOpt.get();

        if (existing.getStatus() == RecipeHistoryStatus.DELETED) {
          existing.active(clock);
          recipeHistoryRepository.save(existing);
          return true;
        }

        return false;
      }

      throw e;
    }
  }

  public Boolean exist(UUID userId, UUID recipeId) {
    return recipeHistoryRepository.existsByRecipeIdAndUserIdAndStatus(
        recipeId, userId, RecipeHistoryStatus.ACTIVE);
  }

  @Transactional
  public RecipeHistory getWithView(UUID userId, UUID recipeId) {
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
    recipeHistory.delete(clock);
    recipeHistoryRepository.save(recipeHistory);
  }

  @Transactional
  public List<RecipeHistory> deleteByRecipe(UUID recipeId) {
    List<RecipeHistory> histories =
        recipeHistoryRepository.findAllByRecipeIdAndStatus(recipeId, RecipeHistoryStatus.ACTIVE);

    histories.forEach(h -> h.delete(clock));
    recipeHistoryRepository.saveAll(histories);

    return histories;
  }

  @Transactional
  public void blockByRecipe(UUID recipeId) {
    List<RecipeHistory> histories = recipeHistoryRepository.findAllByRecipeId(recipeId);
    histories.forEach(recipeHistory -> recipeHistory.block(clock));
    recipeHistoryRepository.saveAll(histories);
  }

  @Deprecated(forRemoval = true)
  public Page<RecipeHistory> getCategorized(UUID userId, UUID categoryId, int page) {
    Pageable pageable = RecipePageRequest.create(page, RecipeHistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, categoryId, RecipeHistoryStatus.ACTIVE, pageable);
  }

  public CursorPage<RecipeHistory> getCategorized(UUID userId, UUID categoryId, String cursor) {
    Pageable pageable = CursorPageable.firstPage();
    Pageable probe = CursorPageable.probe(pageable);
    boolean first = (cursor == null || cursor.isBlank());

    List<RecipeHistory> rows =
        first
            ? recipeHistoryRepository.findCategorizedFirst(
            userId, categoryId, RecipeHistoryStatus.ACTIVE, probe)
            : keyset(userId, categoryId, cursor, probe);

    return CursorPages.of(
        rows,
        pageable.getPageSize(),
        h -> viewedAtCursorCodec.encode(new ViewedAtCursor(h.getViewedAt(), h.getId())));
  }

  private List<RecipeHistory> keyset(UUID userId, UUID categoryId, String cursor, Pageable probe) {
    ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
    return recipeHistoryRepository.findCategorizedKeyset(
        userId, categoryId, RecipeHistoryStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
  }

  @Deprecated(forRemoval = true)
  public Page<RecipeHistory> getUnCategorized(UUID userId, int page) {
    Pageable pageable = RecipePageRequest.create(page, RecipeHistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findAllByUserIdAndRecipeCategoryIdAndStatus(
        userId, null, RecipeHistoryStatus.ACTIVE, pageable);
  }


  public CursorPage<RecipeHistory> getUnCategorized(UUID userId, String cursor) {
    Pageable pageable = CursorPageable.firstPage();
    Pageable probe = CursorPageable.probe(pageable);
    boolean first = (cursor == null || cursor.isBlank());

    List<RecipeHistory> rows =
        first
            ? recipeHistoryRepository.findUncategorizedFirst(
            userId, RecipeHistoryStatus.ACTIVE, probe)
            : keysetUncategorized(userId, cursor, probe);

    return CursorPages.of(
        rows,
        pageable.getPageSize(),
        h -> viewedAtCursorCodec.encode(new ViewedAtCursor(h.getViewedAt(), h.getId())));
  }

  private List<RecipeHistory> keysetUncategorized(UUID userId, String cursor, Pageable probe) {
    ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
    return recipeHistoryRepository.findUncategorizedKeyset(
        userId, RecipeHistoryStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
  }

  @Deprecated(forRemoval = true)
  public Page<RecipeHistory> getRecents(UUID userId, int page) {
    Pageable pageable = RecipePageRequest.create(page, RecipeHistorySort.VIEWED_AT_DESC);
    return recipeHistoryRepository.findByUserIdAndStatus(
        userId, RecipeHistoryStatus.ACTIVE, pageable);
  }

  public CursorPage<RecipeHistory> getRecents(UUID userId, String cursor) {
    Pageable pageable = CursorPageable.firstPage();
    Pageable probe = CursorPageable.probe(pageable);
    boolean first = (cursor == null || cursor.isBlank());

    List<RecipeHistory> rows =
        first
            ? recipeHistoryRepository.findRecentsFirst(userId, RecipeHistoryStatus.ACTIVE, probe)
            : keysetRecents(userId, cursor, probe);

    return CursorPages.of(
        rows,
        pageable.getPageSize(),
        h -> viewedAtCursorCodec.encode(new ViewedAtCursor(h.getViewedAt(), h.getId())));
  }

  private List<RecipeHistory> keysetRecents(UUID userId, String cursor, Pageable probe) {
    ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
    return recipeHistoryRepository.findRecentsKeyset(
        userId, RecipeHistoryStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
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
