package com.cheftory.api.market;

import com.cheftory.api._common.region.MarketContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market")
public class MarketController {

  @GetMapping
  public MarketResponse getMarket() {
    MarketContext.Info info = MarketContext.required();
    return MarketResponse.of(info.market(), info.countryCode());
  }
}
