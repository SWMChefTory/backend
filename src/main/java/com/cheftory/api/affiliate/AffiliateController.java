package com.cheftory.api.affiliate;

import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.dto.AffiliateSearchResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 제휴사 관련 API 요청을 처리하는 컨트롤러.
 *
 * <p>쿠팡 파트너스 API를 통해 제품 검색 기능을 제공합니다.</p>
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/affiliate")
public class AffiliateController {

    private final AffiliateService affiliateService;

    /**
     * 쿠팡에서 제품을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 제품 응답
     * @throws CoupangException 쿠팡 API 요청 실패 시
     */
    @GetMapping("/search/coupang")
    public AffiliateSearchResponse searchProducts(@NotBlank @RequestParam String keyword) throws CoupangException {
        return AffiliateSearchResponse.from(affiliateService.searchCoupangProducts(keyword));
    }
}
