package com.cheftory.api.notification.util;

import com.cheftory.api._common.Clock;
import com.cheftory.api.notification.entity.Notification;
import com.cheftory.api.notification.entity.NotificationContent;
import com.cheftory.api.notification.entity.NotificationMetadata;
import com.cheftory.api.notification.entity.NotificationTarget;
import com.cheftory.api.notification.entity.NotificationType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 레시피 생성 완료 알림 모델 생성 팩토리.
 */
@Component
@RequiredArgsConstructor
public class NotificationMessageFactory {

    private final Clock clock;

    /**
     * 레시피 생성 완료 알림 도메인 모델을 생성합니다.
     *
     * @param userIds 수신 대상 유저 ID 목록
     * @param recipeId 생성 완료된 레시피 ID
     * @param recipeTitle 레시피 제목
     * @return 채널 변환 전 공통 알림 모델
     */
    public Notification create(List<UUID> userIds, UUID recipeId, String recipeTitle) {
        return Notification.of(
                NotificationType.RECIPE_CREATED,
                NotificationContent.of("레시피 생성 완료", buildBody(recipeTitle)),
                NotificationTarget.of(userIds),
                NotificationMetadata.of(
                        recipeId.toString(), "recipe", clock.now().toString()));
    }

    private String buildBody(String recipeTitle) {
        if (recipeTitle == null || recipeTitle.isBlank()) {
            return "레시피 생성이 완료되었어요.";
        }
        return recipeTitle + " 레시피 생성이 완료되었어요.";
    }
}
