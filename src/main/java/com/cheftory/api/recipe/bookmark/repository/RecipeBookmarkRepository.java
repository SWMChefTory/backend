package com.cheftory.api.recipe.bookmark.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;

public interface RecipeBookmarkRepository {
    boolean create(RecipeBookmark recipeBookmark) throws RecipeBookmarkException;

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    boolean recreate(UUID userId, UUID recipeId, Clock clock) throws RecipeBookmarkException;

    boolean exists(UUID userId, UUID recipeId);

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    void categorize(UUID userId, UUID recipeId, UUID categoryId);

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3)
    void unCategorize(UUID categoryId);

    void delete(UUID userId, UUID recipeId, Clock clock) throws RecipeBookmarkException;

    void deletes(List<UUID> recipeBookmarkIds, Clock clock);

    void block(UUID recipeId, Clock clock);

    RecipeBookmark get(UUID userId, UUID recipeId) throws RecipeBookmarkException;

    CursorPage<RecipeBookmark> keysetRecents(UUID userId, String cursor);

    CursorPage<RecipeBookmark> keysetRecentsFirst(UUID userId);

    CursorPage<RecipeBookmark> keysetCategorized(UUID userId, UUID categoryId, String cursor);

    CursorPage<RecipeBookmark> keysetCategorizedFirst(UUID userId, UUID categoryId);

    List<RecipeBookmarkCategorizedCountProjection> countCategorized(List<UUID> categoryIds);

    RecipeBookmarkUnCategorizedCountProjection countUncategorized(UUID userId);

    List<RecipeBookmark> gets(UUID userId, List<UUID> recipeIds);

    List<RecipeBookmark> gets(UUID recipeId);
}
