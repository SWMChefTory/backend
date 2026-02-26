package com.cheftory.api.notification;

import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeCreationNotificationAdapter 테스트")
class RecipeCreationNotificationAdapterTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RecipeCreationNotificationAdapter adapter;

    @Nested
    @DisplayName("레시피 생성 완료 알림 위임 (notifyRecipeCreated)")
    class NotifyRecipeCreated {

        @Test
        @DisplayName("Then - NotificationService로 그대로 위임한다")
        void thenDelegatesToNotificationService() {
            List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            UUID recipeId = UUID.randomUUID();
            String recipeTitle = "김치찌개";

            adapter.notifyRecipeCreated(userIds, recipeId, recipeTitle);

            verify(notificationService).sendRecipeCreated(userIds, recipeId, recipeTitle);
        }
    }
}
