package com.cheftory.api.notification.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.notification.entity.Notification;
import com.cheftory.api.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationMessageFactory 테스트")
class NotificationMessageFactoryTest {

    private final Clock clock = mock(Clock.class);
    private final NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactory(clock);

    private final UUID userId = UUID.randomUUID();
    private final UUID recipeId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 2, 26, 10, 30, 0);

    @Nested
    @DisplayName("레시피 생성 완료 알림 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 레시피 제목이 존재할 때")
        class GivenRecipeTitle {

            @Test
            @DisplayName("Then - 제목/본문/메타데이터가 포함된 Notification을 생성한다")
            void thenCreatesNotification() {
                when(clock.now()).thenReturn(now);

                Notification result = notificationMessageFactory.create(List.of(userId), recipeId, "김치찌개");

                assertThat(result.type()).isEqualTo(NotificationType.RECIPE_CREATED);
                assertThat(result.content().title()).isEqualTo("레시피 생성 완료");
                assertThat(result.content().body()).isEqualTo("김치찌개 레시피 생성이 완료되었어요.");
                assertThat(result.target().userIds()).containsExactly(userId);
                assertThat(result.metadata().targetId()).isEqualTo(recipeId.toString());
                assertThat(result.metadata().targetType()).isEqualTo("recipe");
                assertThat(result.metadata().sentAt()).isEqualTo(now.toString());
            }
        }

        @Nested
        @DisplayName("Given - 레시피 제목이 비어 있을 때")
        class GivenBlankRecipeTitle {

            @Test
            @DisplayName("Then - 기본 본문으로 Notification을 생성한다")
            void thenCreatesNotificationWithFallbackBody() {
                when(clock.now()).thenReturn(now);

                Notification result = notificationMessageFactory.create(List.of(userId), recipeId, "   ");

                assertThat(result.content().body()).isEqualTo("레시피 생성이 완료되었어요.");
            }
        }
    }
}
