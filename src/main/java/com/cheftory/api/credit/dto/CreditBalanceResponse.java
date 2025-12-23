package com.cheftory.api.credit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreditBalanceResponse(@JsonProperty("balance") long balance) {
  public static CreditBalanceResponse from(long balance) {
    return new CreditBalanceResponse(balance);
  }
}
