package com.cheftory.api.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import com.cheftory.api.notification.client.ExpoNotificationClient;
import com.cheftory.api.notification.entity.Notification;
import com.cheftory.api.notification.entity.NotificationContent;
import com.cheftory.api.notification.entity.NotificationMetadata;
import com.cheftory.api.notification.entity.NotificationTarget;
import com.cheftory.api.notification.entity.NotificationType;
import com.cheftory.api.notification.port.NotificationPushTokenPort;
import com.cheftory.api.notification.util.NotificationMessageFactory;
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
@DisplayName("NotificationService 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationPushTokenPort notificationPushTokenPort;

    @Mock
    private NotificationMessageFactory notificationMessageFactory;

    @Mock
    private ExpoNotificationClient expoNotificationClient;

    @InjectMocks
    private NotificationService notificationService;

    private final UUID userId1 = UUID.randomUUID();
    private final UUID userId2 = UUID.randomUUID();
    private final UUID recipeId = UUID.randomUUID();

    @Nested
    @DisplayName("레시피 생성 완료 알림 전송 (sendRecipeCreated)")
    class SendRecipeCreated {

        @Nested
        @DisplayName("Given - 활성 Expo 토큰이 존재할 때")
        class GivenActiveExpoTokens {

            @Test
            @DisplayName("Then - 알림 모델을 생성하고 모든 토큰으로 전송한다")
            void thenCreatesNotificationAndSendsToAllTokens() {
                List<UUID> userIds = List.of(userId1, userId2);
                Notification notification = recipeCreatedNotification(userIds, recipeId, "김치찌개");
                when(notificationMessageFactory.create(userIds, recipeId, "김치찌개"))
                        .thenReturn(notification);
                when(notificationPushTokenPort.findActiveExpoTokens(userIds))
                        .thenReturn(List.of("expo-token-1", "expo-token-2"));

                notificationService.sendRecipeCreated(userIds, recipeId, "김치찌개");

                verify(notificationMessageFactory).create(userIds, recipeId, "김치찌개");
                verify(notificationPushTokenPort).findActiveExpoTokens(userIds);
                verify(expoNotificationClient).send("expo-token-1", notification);
                verify(expoNotificationClient).send("expo-token-2", notification);
            }
        }

        @Nested
        @DisplayName("Given - 활성 Expo 토큰이 없을 때")
        class GivenNoActiveExpoTokens {

            @Test
            @DisplayName("Then - 알림 모델은 생성하지만 전송은 수행하지 않는다")
            void thenDoesNotSend() {
                List<UUID> userIds = List.of(userId1);
                Notification notification = recipeCreatedNotification(userIds, recipeId, "된장찌개");
                when(notificationMessageFactory.create(userIds, recipeId, "된장찌개"))
                        .thenReturn(notification);
                when(notificationPushTokenPort.findActiveExpoTokens(userIds)).thenReturn(List.of());

                notificationService.sendRecipeCreated(userIds, recipeId, "된장찌개");

                verify(expoNotificationClient, never()).send(anyString(), any());
            }
        }

        @Nested
        @DisplayName("Given - 전송 중 예외가 발생할 때")
        class GivenSendException {

            @Test
            @DisplayName("Then - 예외를 전파하지 않고 종료한다")
            void thenSwallowsException() {
                List<UUID> userIds = List.of(userId1);
                Notification notification = recipeCreatedNotification(userIds, recipeId, "비빔밥");
                when(notificationMessageFactory.create(userIds, recipeId, "비빔밥"))
                        .thenReturn(notification);
                when(notificationPushTokenPort.findActiveExpoTokens(userIds)).thenReturn(List.of("expo-token-1"));
                doThrow(new RuntimeException("boom"))
                        .when(expoNotificationClient)
                        .send("expo-token-1", notification);

                assertDoesNotThrow(() -> notificationService.sendRecipeCreated(userIds, recipeId, "비빔밥"));
            }
        }
    }

    private Notification recipeCreatedNotification(List<UUID> userIds, UUID recipeId, String title) {
        return Notification.of(
                NotificationType.RECIPE_CREATED,
                NotificationContent.of("레시피 생성 완료", title + " 레시피 생성이 완료되었어요."),
                NotificationTarget.of(userIds),
                NotificationMetadata.of(recipeId.toString(), "recipe", "2025-01-01T00:00"));
    }
}
