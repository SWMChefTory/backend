package com.cheftory.api.market;

import com.cheftory.api._common.region.Market;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MarketResponse(
    @JsonProperty("market") String market, @JsonProperty("country_code") String countryCode) {
  public static MarketResponse of(Market market, String countryCode) {
    return new MarketResponse(market.name(), countryCode);
  }
}
