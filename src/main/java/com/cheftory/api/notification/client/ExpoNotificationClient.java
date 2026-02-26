package com.cheftory.api.notification.client;

import com.cheftory.api.notification.client.dto.ExpoNotificationSendRequest;
import com.cheftory.api.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Expo Push API 클라이언트.
 *
 * <p>공통 알림 모델을 Expo 전송 요청 DTO로 변환하여 외부 API로 전송합니다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpoNotificationClient {

    private final ExpoNotificationHttpApi expoNotificationHttpApi;
    private final ExpoNotificationProperties expoNotificationProperties;

    /**
     * 단일 Expo 토큰으로 알림을 전송합니다.
     *
     * @param expoToken Expo 푸시 토큰
     * @param notification 공통 알림 모델
     */
    public void send(String expoToken, Notification notification) {
        if (!expoNotificationProperties.isEnabled()) {
            log.debug("Expo push disabled.");
            return;
        }

        try {
            ExpoNotificationSendRequest request = ExpoNotificationSendRequest.from(expoToken, notification);
            String response = expoNotificationHttpApi.send(request);
            log.info(
                    "Expo push sent. action={}, targetId={}, token={}, response={}",
                    request.data().action(),
                    request.data().targetId(),
                    mask(expoToken),
                    response);
        } catch (WebClientException e) {
            log.error("Expo Push API 요청 중 WebClient 오류 발생 - type={}", notification.type(), e);
            throw e;
        } catch (Exception e) {
            log.error("Expo Push API 요청 중 알 수 없는 오류 발생 - type={}", notification.type(), e);
            throw e;
        }
    }

    private String mask(String token) {
        if (token == null || token.length() < 12) return "***";
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}
