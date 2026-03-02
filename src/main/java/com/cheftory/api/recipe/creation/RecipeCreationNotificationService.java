package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.creation.notification.RecipeCreationNotificationPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCreationNotificationService {

    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeDetailMetaService recipeDetailMetaService;
    private final RecipeCreationNotificationPort recipeCreationNotificationPort;

    public void notify(UUID recipeId) throws RecipeDetailMetaException {
        String recipeTitle = recipeDetailMetaService.get(recipeId).getTitle();
        List<UUID> userIds = recipeBookmarkService.gets(recipeId).stream()
                .map(RecipeBookmark::getUserId)
                .distinct()
                .toList();

        if (userIds.isEmpty()) return;

        recipeCreationNotificationPort.notifyRecipeCreated(userIds, recipeId, recipeTitle);
    }
}
