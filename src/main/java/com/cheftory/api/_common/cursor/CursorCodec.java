package com.cheftory.api._common.cursor;

public interface CursorCodec<T> {
    T decode(String cursor) throws CursorException;

    String encode(T value) throws CursorException;
}
