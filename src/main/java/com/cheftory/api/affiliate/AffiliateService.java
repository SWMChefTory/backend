package com.cheftory.api.affiliate;

import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProducts;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 제휴사 관련 비즈니스 로직을 처리하는 서비스.
 *
 * <p>쿠팡 파트너스 API를 통한 제품 검색 및 캐싱을 담당합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class AffiliateService {

    private final CoupangClient client;

    /**
     * 쿠팡 파트너스 API를 통해 제품을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 쿠팡 제품 목록
     * @throws CoupangException 쿠팡 API 요청 실패 시
     */
    @Cacheable(value = "coupangSearchCache", key = "#keyword")
    public CoupangProducts searchCoupangProducts(String keyword) throws CoupangException {
        return client.searchProducts(keyword);
    }
}
