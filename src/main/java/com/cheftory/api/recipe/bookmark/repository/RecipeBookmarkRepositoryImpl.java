package com.cheftory.api.recipe.bookmark.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.*;
import com.cheftory.api.recipe.bookmark.entity.*;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkErrorCode;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeBookmarkRepositoryImpl implements RecipeBookmarkRepository {

    private final RecipeBookmarkJpaRepository repository;
    private final ViewedAtCursorCodec viewedAtCursorCodec;

    @Override
    public boolean create(RecipeBookmark recipeBookmark) throws RecipeBookmarkException {
        try {
            repository.save(recipeBookmark);
            return true;
        } catch (DataIntegrityViolationException e) {
            throw new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_ALREADY_EXISTS);
        }
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    @Override
    public boolean recreate(UUID userId, UUID recipeId, Clock clock) throws RecipeBookmarkException {
        RecipeBookmark existing = repository
                .findByUserIdAndRecipeId(userId, recipeId)
                .orElseThrow(() -> new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND));

        if (existing.getStatus() == RecipeBookmarkStatus.DELETED) {
            existing.active(clock);
            repository.save(existing);
            return true;
        }
        return false;
    }

    @Recover
    public boolean recover(ObjectOptimisticLockingFailureException e, UUID userId, UUID recipeId, Clock clock) {
        log.warn("recreate optimistic lock fail userId={}, recipeId={}", userId, recipeId, e);
        throw new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_CREATE_FAIL);
    }

    @Override
    public boolean exists(UUID userId, UUID recipeId) {
        return repository.existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE);
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    @Override
    public void categorize(UUID userId, UUID recipeId, UUID categoryId) {
        RecipeBookmark bookmark = repository
                .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE)
                .orElseThrow(() -> new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND));
        bookmark.updateRecipeCategoryId(categoryId);
        repository.save(bookmark);
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    @Override
    public void unCategorize(UUID categoryId) {
        List<RecipeBookmark> viewStatuses =
                repository.findByRecipeCategoryIdAndStatus(categoryId, RecipeBookmarkStatus.ACTIVE);
        viewStatuses.forEach(RecipeBookmark::emptyRecipeCategoryId);
        repository.saveAll(viewStatuses);
    }

    @Override
    public void delete(UUID userId, UUID recipeId, Clock clock) {
        RecipeBookmark bookmark = repository
                .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE)
                .orElseThrow(() -> new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND));
        bookmark.delete(clock);
        repository.save(bookmark);
    }

    @Override
    public void deletes(List<UUID> recipeBookmarkIds, Clock clock) {
        List<RecipeBookmark> bookmarks = repository.findAllByIdIn(recipeBookmarkIds);

        bookmarks.forEach(h -> h.delete(clock));
        repository.saveAll(bookmarks);
    }

    @Override
    public void block(UUID recipeId, Clock clock) {
        List<RecipeBookmark> bookmarks = repository.findAllByRecipeId(recipeId);
        bookmarks.forEach(bookmark -> bookmark.block(clock));
        repository.saveAll(bookmarks);
    }

    @Override
    public RecipeBookmark get(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        return repository
                .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeBookmarkStatus.ACTIVE)
                .orElseThrow(() -> new RecipeBookmarkException(RecipeBookmarkErrorCode.RECIPE_BOOKMARK_NOT_FOUND));
    }

    @Override
    public CursorPage<RecipeBookmark> keysetRecents(UUID userId, String cursor) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);

        ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
        List<RecipeBookmark> bookmarks =
                repository.findRecentsKeyset(userId, RecipeBookmarkStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
        return toCursorPage(bookmarks, pageable);
    }

    @Override
    public CursorPage<RecipeBookmark> keysetRecentsFirst(UUID userId) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);

        List<RecipeBookmark> bookmarks = repository.findRecentsFirst(userId, RecipeBookmarkStatus.ACTIVE, probe);
        return toCursorPage(bookmarks, pageable);
    }

    @Override
    public CursorPage<RecipeBookmark> keysetCategorized(UUID userId, UUID categoryId, String cursor) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);

        ViewedAtCursor p = viewedAtCursorCodec.decode(cursor);
        List<RecipeBookmark> bookmarks = repository.findCategorizedKeyset(
                userId, categoryId, RecipeBookmarkStatus.ACTIVE, p.lastViewedAt(), p.lastId(), probe);
        return toCursorPage(bookmarks, pageable);
    }

    @Override
    public CursorPage<RecipeBookmark> keysetCategorizedFirst(UUID userId, UUID categoryId) {
        Pageable pageable = CursorPageable.firstPage();
        Pageable probe = CursorPageable.probe(pageable);

        List<RecipeBookmark> bookmarks =
                repository.findCategorizedFirst(userId, categoryId, RecipeBookmarkStatus.ACTIVE, probe);

        return toCursorPage(bookmarks, pageable);
    }

    public List<RecipeBookmarkCategorizedCountProjection> countCategorized(List<UUID> categoryIds) {
        return repository.countByCategoryIdsAndStatus(categoryIds, RecipeBookmarkStatus.ACTIVE);
    }

    @Override
    public RecipeBookmarkUnCategorizedCountProjection countUncategorized(UUID userId) {
        return repository.countUncategorizedByUserIdAndStatus(userId, RecipeBookmarkStatus.ACTIVE);
    }

    @Override
    public List<RecipeBookmark> gets(UUID userId, List<UUID> recipeIds) {
        return repository.findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeBookmarkStatus.ACTIVE);
    }

    @Override
    public List<RecipeBookmark> gets(UUID recipeId) {
        return repository.findAllByRecipeIdAndStatus(recipeId, RecipeBookmarkStatus.ACTIVE);
    }

    private CursorPage<RecipeBookmark> toCursorPage(List<RecipeBookmark> bookmarks, Pageable pageable) {
        return CursorPages.of(
                bookmarks,
                pageable.getPageSize(),
                b -> viewedAtCursorCodec.encode(new ViewedAtCursor(b.getViewedAt(), b.getId())));
    }
}
