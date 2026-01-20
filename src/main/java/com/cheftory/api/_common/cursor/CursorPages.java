package com.cheftory.api._common.cursor;

import java.util.List;
import java.util.function.Function;

public final class CursorPages {
    private CursorPages() {}

    public static <T> CursorPage<T> of(List<T> rows, int limit, Function<T, String> cursorOf) {
        boolean hasNext = rows.size() > limit;
        List<T> items = hasNext ? rows.subList(0, limit) : rows;

        String nextCursor = hasNext && !items.isEmpty() ? cursorOf.apply(items.getLast()) : null;

        return CursorPage.of(items, nextCursor);
    }
}
