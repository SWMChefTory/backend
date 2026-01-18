package com.cheftory.api._common.cursor;

import java.util.List;

public record CursorPage<T>(List<T> items, String nextCursor) {
  public boolean hasNext() {
    return nextCursor != null && !nextCursor.isBlank();
  }

  public static <T> CursorPage<T> of(List<T> items, String nextCursor) {
    return new CursorPage<>(items, nextCursor);
  }
}
