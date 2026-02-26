package com.cheftory.api.recipe.creation.notification;

import java.util.List;
import java.util.UUID;

/**
 * 레시피 생성 알림 포트.
 */
public interface RecipeCreationNotificationPort {

    void notifyRecipeCreated(List<UUID> userIds, UUID recipeId, String recipeTitle);
}
