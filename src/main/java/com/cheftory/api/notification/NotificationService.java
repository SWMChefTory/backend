package com.cheftory.api.notification;

import com.cheftory.api.notification.client.ExpoNotificationClient;
import com.cheftory.api.notification.entity.Notification;
import com.cheftory.api.notification.port.NotificationPushTokenPort;
import com.cheftory.api.notification.util.NotificationMessageFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 도메인 오케스트레이션 서비스.
 *
 * <p>알림 모델을 생성하고 수신자 토큰 조회 후 채널(Expo)로 전송합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPushTokenPort notificationPushTokenPort;
    private final NotificationMessageFactory notificationMessageFactory;
    private final ExpoNotificationClient expoNotificationClient;

    /**
     * 레시피 생성 완료 알림을 전송합니다.
     *
     * @param userIds 알림 수신 대상 유저 ID 목록
     * @param recipeId 생성 완료된 레시피 ID
     * @param recipeTitle 레시피 제목
     */
    public void sendRecipeCreated(List<UUID> userIds, UUID recipeId, String recipeTitle) {
        try {
            Notification notification = notificationMessageFactory.create(userIds, recipeId, recipeTitle);
            List<String> expoTokens = notificationPushTokenPort.findActiveExpoTokens(
                    notification.target().userIds());
            if (expoTokens.isEmpty()) return;

            for (String expoToken : expoTokens) {
                expoNotificationClient.send(expoToken, notification);
            }
        } catch (Exception e) {
            log.warn("레시피 생성 완료 푸시 발송 실패: recipeId={}", recipeId, e);
        }
    }
}
