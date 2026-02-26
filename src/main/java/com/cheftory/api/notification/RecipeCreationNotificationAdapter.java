package com.cheftory.api.notification;

import com.cheftory.api.recipe.creation.notification.RecipeCreationNotificationPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 레시피 생성 도메인 포트를 notification 서비스로 연결하는 어댑터.
 */
@Component
@RequiredArgsConstructor
public class RecipeCreationNotificationAdapter implements RecipeCreationNotificationPort {

    private final NotificationService service;

    /**
     * 레시피 생성 완료 알림 요청을 notification 서비스로 위임합니다.
     */
    @Override
    public void notifyRecipeCreated(List<UUID> userIds, UUID recipeId, String recipeTitle) {
        service.sendRecipeCreated(userIds, recipeId, recipeTitle);
    }
}
