package com.cheftory.api.recipe.bookmark;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.CursorPageable;
import com.cheftory.api._common.cursor.CursorPages;
import com.cheftory.api._common.cursor.ViewedAtCursor;
import com.cheftory.api._common.cursor.ViewedAtCursorCodec;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkStatus;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeBookmarkService {
    private final RecipeBookmarkRepository recipeBookmarkRepository;
    private final ViewedAtCursorCodec viewedAtCursorCodec;
    private final Clock clock;

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    public boolean create(UUID userId, UUID recipeId) {
        try {
            recipeBookmarkRepository.save(RecipeBookmark.create(clock, userId, recipeId));
            return true;
        } catch (DataIntegrityViolationException e) {
            var existingOpt = recipeBookmarkRepository.findByUserIdAndRecipeId(userId, recipeId);

            if (existingOpt.isPresent()) {
                RecipeBookmark existing = existingOpt.get();

                if (existing.getStatus() == RecipeBookmarkStatus.DELETED) {
                    existing.active(clock);
                    recipeBookmarkRepository.save(existing);
                    return true;
                }

                return false;
            }

            throw e;
        }
    }

    public boolean exist(UUID userId, UUID recipeId) {
        return recipeBookmarkRepository.existsByRecipeIdAndUserIdAndStatus(
                recipeId, userId, RecipeBookmarkStatus.ACTIVE);
    }

    @Transactional
    @Nullable
    public RecipeBookmark getWithView(UUID userId, UUID recipeId) {
        return recipeBookmarkRepository
                .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE)
                .map(bookmark -> {
                    bookmark.updateViewedAt(clock);
                    return recipeBookmarkRepository.save(bookmark);
                })
                .orElse(null);
    }

    @Transactional
    public void updateCategory(UUID userId, UUID recipeId, UUID categoryId) {
        RecipeBookmark bookmark = recipeBookmarkRepository
                .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE)
                .orElseThrow(() -> new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND));
        bookmark.updateRecipeCategoryId(categoryId);
        recipeBookmarkRepository.save(bookmark);
    }

    @Transactional
    public void unCategorize(UUID categoryId) {
        List<RecipeBookmark> viewStatuses =
                recipeBookmarkRepository.findByRecipeCategoryIdAndStatus(categoryId, RecipeBookmarkStatus.ACTIVE);
        viewStatuses.forEach(RecipeBookmark::emptyRecipeCategoryId);
        recipeBookmarkRepository.saveAll(viewStatuses);
    }

    @Transactional
    public void delete(UUID userId, UUID recipeId) {
        RecipeBookmark bookmark = recipeBookmarkRepository
                .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE)
                .orElseThrow(() -> new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND));
        bookmark.delete(clock);
        recipeBookmarkRepository.save(bookmark);
    }

    @Transactional
    public List<RecipeBookmark> deleteByRecipe(UUID recipeId) {
        List<RecipeBookmark> bookmarks =
                recipeBookmarkRepository.findAllByRecipeIdAndStatus(recipeId, RecipeBookmarkStatus.ACTIVE);

        bookmarks.forEach(h -> h.delete(clock));
        recipeBookmarkRepository.saveAll(bookmarks);

        return bookmarks;
    }

    @Transactional
    public void blockByRecipe(UUID recipeId) {
        List<RecipeBookmark> bookmarks = recipeBookmarkRepository.findAllByRecipeId(recipeId);
        bookmarks.forEach(bookmark -> bookmark.block(clock));
        recipeBookmarkRepository.saveAll(bookmarks);
    }

    public CursorPage<RecipeBookmark> getCategorized(UUID userId, UUID categoryId, String cursor) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        boolean first = (cursor == null || cursor.isBlank());

        List<RecipeBookmark> rows = first
                ? recipeBookmarkRepository.findCategorizedFirst(userId, categoryId, RecipeBookmarkStatus.ACTIVE, probe)
                : keyset(userId, categoryId, cursor, probe);

        return CursorPages.of(
                rows,
                pageable.getPageSize(),
                h -> viewedAtCursorCodec.encode(new ViewedAtCursor(h.getViewedAt(), h.getId())));
    }

    private List<RecipeBookmark> keyset(UUID userId, UUID categoryId, String cursor, Pageable probe) {
        ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
        return recipeBookmarkRepository.findCategorizedKeyset(
                userId, categoryId, RecipeBookmarkStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
    }

    public CursorPage<RecipeBookmark> getUnCategorized(UUID userId, String cursor) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        boolean first = (cursor == null || cursor.isBlank());

        List<RecipeBookmark> rows = first
                ? recipeBookmarkRepository.findUncategorizedFirst(userId, RecipeBookmarkStatus.ACTIVE, probe)
                : keysetUncategorized(userId, cursor, probe);

        return CursorPages.of(
                rows,
                pageable.getPageSize(),
                h -> viewedAtCursorCodec.encode(new ViewedAtCursor(h.getViewedAt(), h.getId())));
    }

    private List<RecipeBookmark> keysetUncategorized(UUID userId, String cursor, Pageable probe) {
        ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
        return recipeBookmarkRepository.findUncategorizedKeyset(
                userId, RecipeBookmarkStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
    }

    public CursorPage<RecipeBookmark> getRecents(UUID userId, String cursor) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);
        boolean first = (cursor == null || cursor.isBlank());

        List<RecipeBookmark> rows = first
                ? recipeBookmarkRepository.findRecentsFirst(userId, RecipeBookmarkStatus.ACTIVE, probe)
                : keysetRecents(userId, cursor, probe);

        return CursorPages.of(
                rows,
                pageable.getPageSize(),
                h -> viewedAtCursorCodec.encode(new ViewedAtCursor(h.getViewedAt(), h.getId())));
    }

    private List<RecipeBookmark> keysetRecents(UUID userId, String cursor, Pageable probe) {
        ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
        return recipeBookmarkRepository.findRecentsKeyset(
                userId, RecipeBookmarkStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
    }

    public List<RecipeBookmarkCategorizedCount> countByCategories(List<UUID> categoryIds) {
        List<RecipeBookmarkCategorizedCountProjection> projections =
                recipeBookmarkRepository.countByCategoryIdsAndStatus(categoryIds, RecipeBookmarkStatus.ACTIVE);
        return projections.stream()
                .map(projection -> RecipeBookmarkCategorizedCount.of(
                        projection.getCategoryId(), projection.getCount().intValue()))
                .toList();
    }

    public RecipeBookmarkUnCategorizedCount countUncategorized(UUID userId) {
        RecipeBookmarkUnCategorizedCountProjection projection =
                recipeBookmarkRepository.countByUserIdAndStatus(userId, RecipeBookmarkStatus.ACTIVE);
        return RecipeBookmarkUnCategorizedCount.of(projection.getCount().intValue());
    }

    public List<RecipeBookmark> getByRecipes(List<UUID> recipeIds, UUID userId) {
        return recipeBookmarkRepository.findByRecipeIdInAndUserIdAndStatus(
                recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
    }
}
