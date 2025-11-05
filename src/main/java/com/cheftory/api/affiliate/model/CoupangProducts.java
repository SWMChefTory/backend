package com.cheftory.api.affiliate.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CoupangProducts {
  private List<CoupangProduct> coupangProducts;

  public static CoupangProducts of(List<CoupangProduct> coupangProducts) {
    return CoupangProducts.builder().coupangProducts(coupangProducts).build();
  }
}
