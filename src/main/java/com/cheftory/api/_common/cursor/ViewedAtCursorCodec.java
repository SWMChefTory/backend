package com.cheftory.api._common.cursor;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ViewedAtCursorCodec implements CursorCodec<ViewedAtCursor> {

    private static final String SEP = "|";

    public ViewedAtCursor decode(String cursor) {
        int idx = cursor.lastIndexOf(SEP);

        LocalDateTime t = LocalDateTime.parse(cursor.substring(0, idx));
        UUID id = UUID.fromString(cursor.substring(idx + 1));
        return new ViewedAtCursor(t, id);
    }

    @Override
    public String encode(ViewedAtCursor value) {
        return value.lastViewedAt().toString() + SEP + value.lastId().toString();
    }
}
