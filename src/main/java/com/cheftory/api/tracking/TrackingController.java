package com.cheftory.api.tracking;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.tracking.dto.TrackingClickRequest;
import com.cheftory.api.tracking.dto.TrackingImpressionRequest;
import com.cheftory.api.tracking.exception.TrackingException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 추적 API 컨트롤러.
 *
 * <p>프론트엔드에서 수집한 노출/클릭 데이터를 수신합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * 레시피 노출 배치 기록.
     *
     * @param request 노출 배치 요청
     * @param userId 사용자 ID
     * @return 성공 응답
     * @throws TrackingException 추적 관련 예외
     */
    @PostMapping("/impressions")
    public SuccessOnlyResponse trackImpressions(
            @RequestBody @Valid TrackingImpressionRequest request, @UserPrincipal UUID userId)
            throws TrackingException {
        trackingService.saveImpressions(userId, request);
        return SuccessOnlyResponse.create();
    }

    /**
     * 레시피 클릭 단건 기록.
     *
     * @param request 클릭 요청
     * @param userId 사용자 ID
     * @return 성공 응답
     * @throws TrackingException 추적 관련 예외
     */
    @PostMapping("/clicks")
    public SuccessOnlyResponse trackClick(@RequestBody @Valid TrackingClickRequest request, @UserPrincipal UUID userId)
            throws TrackingException {
        trackingService.saveClick(userId, request);
        return SuccessOnlyResponse.create();
    }
}
