package com.cheftory.api.recipeaccess.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class SimpleAccessInfosResponse {
  private List<SimpleAccessInfo> viewStateInfos;

  public static SimpleAccessInfosResponse from(List<SimpleAccessInfo> simpleAccessInfos) {
    return SimpleAccessInfosResponse.builder()
        .viewStateInfos(simpleAccessInfos)
        .build();
  }
}
