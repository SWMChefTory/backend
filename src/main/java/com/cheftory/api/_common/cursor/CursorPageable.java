package com.cheftory.api._common.cursor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class CursorPageable {
  private static final int CURSOR_PAGE_SIZE = 20;

  private CursorPageable() {}

  public static Pageable firstPage() {
    return PageRequest.of(0, CURSOR_PAGE_SIZE + 1);
  }
}
