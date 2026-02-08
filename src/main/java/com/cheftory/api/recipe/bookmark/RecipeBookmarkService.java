package com.cheftory.api.recipe.bookmark;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeBookmarkService {
    private final RecipeBookmarkRepository recipeBookmarkRepository;
    private final Clock clock;

    public boolean create(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        try {
            recipeBookmarkRepository.create(RecipeBookmark.create(clock, userId, recipeId));
            return true;
        } catch (RecipeBookmarkException e) {
            return recipeBookmarkRepository.recreate(userId, recipeId, clock);
        }
    }

    public boolean exist(UUID userId, UUID recipeId) {
        return recipeBookmarkRepository.exists(userId, recipeId);
    }

    public RecipeBookmark get(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        return recipeBookmarkRepository.get(userId, recipeId);
    }

    public void categorize(UUID userId, UUID recipeId, UUID categoryId) {
        recipeBookmarkRepository.categorize(userId, recipeId, categoryId);
    }

    public void unCategorize(UUID categoryId) {
        recipeBookmarkRepository.unCategorize(categoryId);
    }

    public void delete(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        recipeBookmarkRepository.delete(userId, recipeId, clock);
    }

    public void deletes(List<UUID> recipeBookmarkIds) {
        recipeBookmarkRepository.deletes(recipeBookmarkIds, clock);
    }

    public List<RecipeBookmark> gets(UUID recipeId) {
        return recipeBookmarkRepository.gets(recipeId);
    }

    public void block(UUID recipeId) {
        recipeBookmarkRepository.block(recipeId, clock);
    }

    public CursorPage<RecipeBookmark> getCategorized(UUID userId, UUID categoryId, String cursor) {
        boolean first = (cursor == null || cursor.isBlank());

        return first
                ? recipeBookmarkRepository.keysetCategorizedFirst(userId, categoryId)
                : recipeBookmarkRepository.keysetCategorized(userId, categoryId, cursor);
    }

    public CursorPage<RecipeBookmark> getRecents(UUID userId, String cursor) {
        boolean first = (cursor == null || cursor.isBlank());

        return first
                ? recipeBookmarkRepository.keysetRecentsFirst(userId)
                : recipeBookmarkRepository.keysetRecents(userId, cursor);
    }

    public List<RecipeBookmarkCategorizedCount> countByCategories(List<UUID> categoryIds) {
        List<RecipeBookmarkCategorizedCountProjection> projections =
                recipeBookmarkRepository.countCategorized(categoryIds);
        return projections.stream()
                .map(projection -> RecipeBookmarkCategorizedCount.of(
                        projection.getCategoryId(), projection.getCount().intValue()))
                .toList();
    }

    public RecipeBookmarkUnCategorizedCount countUncategorized(UUID userId) {
        RecipeBookmarkUnCategorizedCountProjection projection = recipeBookmarkRepository.countUncategorized(userId);
        return RecipeBookmarkUnCategorizedCount.of(projection.getCount().intValue());
    }

    public List<RecipeBookmark> gets(List<UUID> recipeIds, UUID userId) {
        return recipeBookmarkRepository.gets(userId, recipeIds);
    }
}
