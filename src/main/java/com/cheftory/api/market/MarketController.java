package com.cheftory.api.market;

import com.cheftory.api._common.region.MarketContext;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마켓 관련 API 요청을 처리하는 컨트롤러.
 *
 * <p>현재 요청의 마켓 정보를 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    /**
     * 현재 마켓 정보를 조회합니다.
     *
     * @return 마켓 정보 응답
     */
    @SneakyThrows
    @GetMapping
    public MarketResponse getMarket() {
        MarketContext.Info info = MarketContext.required();
        return MarketResponse.of(info.market(), info.countryCode());
    }
}
