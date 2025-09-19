package com.cheftory.api.recipeinfo.viewstatus;

import org.springframework.data.domain.Sort;

public final class ViewStatusSort {
  public static final Sort VIEWED_AT_DESC = Sort.by(Sort.Direction.DESC, "viewedAt");
}
