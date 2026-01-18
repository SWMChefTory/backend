package com.cheftory.api._common.cursor;

public interface CursorCodec<T> {
  T decode(String cursor);

  String encode(T value);
}
