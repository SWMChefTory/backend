package com.cheftory.api.market;

import com.cheftory.api._common.region.MarketContext;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    @SneakyThrows
    @GetMapping
    public MarketResponse getMarket() {
        MarketContext.Info info = MarketContext.required();
        return MarketResponse.of(info.market(), info.countryCode());
    }
}
