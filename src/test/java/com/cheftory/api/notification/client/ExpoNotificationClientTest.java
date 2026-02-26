package com.cheftory.api.notification.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api.notification.client.dto.ExpoNotificationSendRequest;
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
import org.mockito.ArgumentCaptor;

@DisplayName("ExpoNotificationClient 테스트")
class ExpoNotificationClientTest {

    private final ExpoNotificationHttpApi expoNotificationHttpApi = mock(ExpoNotificationHttpApi.class);
    private final ExpoNotificationProperties expoNotificationProperties = new ExpoNotificationProperties();
    private final ExpoNotificationClient sut =
            new ExpoNotificationClient(expoNotificationHttpApi, expoNotificationProperties);

    @Nested
    @DisplayName("알림 전송 (send)")
    class Send {

        @Nested
        @DisplayName("Given - Expo 전송이 비활성화일 때")
        class GivenDisabled {

            @Test
            @DisplayName("Then - 외부 API를 호출하지 않는다")
            void thenDoesNotCallExpoApi() {
                expoNotificationProperties.setEnabled(false);

                sut.send("ExponentPushToken[test]", recipeCreatedNotification());

                verifyNoInteractions(expoNotificationHttpApi);
            }
        }

        @Nested
        @DisplayName("Given - Expo 전송이 활성화일 때")
        class GivenEnabled {

            @Test
            @DisplayName("Then - Notification을 Expo 요청 DTO로 변환해 전송한다")
            void thenSendsMappedRequest() {
                expoNotificationProperties.setEnabled(true);
                when(expoNotificationHttpApi.send(any())).thenReturn("{\"data\":\"ok\"}");

                sut.send("ExponentPushToken[test]", recipeCreatedNotification());

                ArgumentCaptor<ExpoNotificationSendRequest> captor =
                        ArgumentCaptor.forClass(ExpoNotificationSendRequest.class);
                verify(expoNotificationHttpApi).send(captor.capture());
                ExpoNotificationSendRequest request = captor.getValue();

                assertThat(request.to()).isEqualTo("ExponentPushToken[test]");
                assertThat(request.title()).isEqualTo("레시피 생성 완료");
                assertThat(request.body()).isEqualTo("김치찌개 레시피 생성이 완료되었어요.");
                assertThat(request.sound()).isEqualTo("default");
                assertThat(request.data().action()).isEqualTo("RECIPE_CREATED");
                assertThat(request.data().targetId()).isEqualTo("recipe-1");
                assertThat(request.data().type()).isEqualTo("recipe");
            }

            @Test
            @DisplayName("Then - 외부 API 예외는 다시 던진다")
            void thenRethrowsException() {
                expoNotificationProperties.setEnabled(true);
                RuntimeException boom = new RuntimeException("boom");
                when(expoNotificationHttpApi.send(any())).thenThrow(boom);

                RuntimeException result = assertThrows(
                        RuntimeException.class, () -> sut.send("ExponentPushToken[test]", recipeCreatedNotification()));

                assertThat(result).isSameAs(boom);
            }
        }
    }

    private Notification recipeCreatedNotification() {
        return Notification.of(
                NotificationType.RECIPE_CREATED,
                NotificationContent.of("레시피 생성 완료", "김치찌개 레시피 생성이 완료되었어요."),
                NotificationTarget.of(List.of(UUID.randomUUID())),
                NotificationMetadata.of("recipe-1", "recipe", "2026-02-26T10:30"));
    }
}
