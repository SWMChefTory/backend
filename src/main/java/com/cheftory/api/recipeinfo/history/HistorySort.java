package com.cheftory.api.recipeinfo.history;

import org.springframework.data.domain.Sort;

public final class HistorySort {
  public static final Sort VIEWED_AT_DESC = Sort.by(Sort.Direction.DESC, "viewedAt");
}
