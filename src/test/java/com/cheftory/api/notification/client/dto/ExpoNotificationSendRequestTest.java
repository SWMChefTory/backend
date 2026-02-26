package com.cheftory.api.notification.client.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.notification.entity.Notification;
import com.cheftory.api.notification.entity.NotificationContent;
import com.cheftory.api.notification.entity.NotificationMetadata;
import com.cheftory.api.notification.entity.NotificationTarget;
import com.cheftory.api.notification.entity.NotificationType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExpoNotificationSendRequest 테스트")
class ExpoNotificationSendRequestTest {

    @Nested
    @DisplayName("변환 (from)")
    class From {

        @Nested
        @DisplayName("Given - 레시피 생성 완료 Notification일 때")
        class GivenRecipeCreatedNotification {

            @Test
            @DisplayName("Then - Expo 전송 요청 DTO로 변환한다")
            void thenMapsToExpoRequest() {
                UUID userId = UUID.randomUUID();
                String token = "ExponentPushToken[test-token]";
                Notification notification = Notification.of(
                        NotificationType.RECIPE_CREATED,
                        NotificationContent.of("레시피 생성 완료", "김치찌개 레시피 생성이 완료되었어요."),
                        NotificationTarget.of(List.of(userId)),
                        NotificationMetadata.of("recipe-1", "recipe", "2026-02-26T10:30"));

                ExpoNotificationSendRequest result = ExpoNotificationSendRequest.from(token, notification);

                assertThat(result.to()).isEqualTo(token);
                assertThat(result.title()).isEqualTo("레시피 생성 완료");
                assertThat(result.body()).isEqualTo("김치찌개 레시피 생성이 완료되었어요.");
                assertThat(result.sound()).isEqualTo("default");
                assertThat(result.data().action()).isEqualTo("RECIPE_CREATED");
                assertThat(result.data().targetId()).isEqualTo("recipe-1");
                assertThat(result.data().type()).isEqualTo("recipe");
                assertThat(result.data().sentAt()).isEqualTo("2026-02-26T10:30");
            }
        }
    }
}
