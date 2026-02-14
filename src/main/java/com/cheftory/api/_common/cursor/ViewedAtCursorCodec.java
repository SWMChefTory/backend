package com.cheftory.api._common.cursor;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ViewedAtCursorCodec extends AbstractCursorCodecSupport implements CursorCodec<ViewedAtCursor> {

    private static final String SEP = "|";

    public ViewedAtCursor decode(String cursor) throws CursorException {
        try {
            requireNonBlank(cursor);
            int separatorIndex = cursor.lastIndexOf(SEP);
            requireSeparatorInMiddle(separatorIndex, cursor.length());

            LocalDateTime lastViewedAt = LocalDateTime.parse(cursor.substring(0, separatorIndex));
            UUID id = UUID.fromString(cursor.substring(separatorIndex + 1));
            return new ViewedAtCursor(lastViewedAt, id);
        } catch (Exception e) {
            throw invalidCursor(e);
        }
    }

    @Override
    public String encode(ViewedAtCursor value) {
        return value.lastViewedAt().toString() + SEP + value.lastId().toString();
    }
}
