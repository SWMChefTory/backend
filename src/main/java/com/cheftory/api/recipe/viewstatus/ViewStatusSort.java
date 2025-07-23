package com.cheftory.api.recipe.viewstatus;
import org.springframework.data.domain.Sort;

public class ViewStatusSort {
  public static final Sort VIEWED_AT_DESC = Sort.by(Sort.Direction.DESC, "viewedAt");
}
