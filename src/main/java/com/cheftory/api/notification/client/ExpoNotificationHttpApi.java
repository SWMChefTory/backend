package com.cheftory.api.notification.client;

import com.cheftory.api.notification.client.dto.ExpoNotificationSendRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Expo Push API HTTP 인터페이스.
 */
@HttpExchange
public interface ExpoNotificationHttpApi {

    /**
     * Expo Push API로 단건 전송 요청을 보냅니다.
     *
     * @param request Expo 전송 요청 DTO
     * @return Expo API 응답 본문
     */
    @PostExchange
    String send(@RequestBody ExpoNotificationSendRequest request);
}
