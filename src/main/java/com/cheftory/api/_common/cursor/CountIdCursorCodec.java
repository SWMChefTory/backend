package com.cheftory.api._common.cursor;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * CountIdCursor 커서를 인코딩/디코딩하는 코덱.
 */
@Component
public class CountIdCursorCodec extends AbstractCursorCodecSupport implements CursorCodec<CountIdCursor> {
    private static final String SEP = "|";

    @Override
    public CountIdCursor decode(String cursor) throws CursorException {

        try {
            requireNonBlank(cursor);
            int separatorIndex = cursor.lastIndexOf(SEP);
            requireSeparatorInMiddle(separatorIndex, cursor.length());
            long lastCount = Long.parseLong(cursor.substring(0, separatorIndex));
            UUID id = UUID.fromString(cursor.substring(separatorIndex + 1));
            return new CountIdCursor(lastCount, id);
        } catch (Exception e) {
            throw invalidCursor(e);
        }
    }

    @Override
    public String encode(CountIdCursor value) {
        return value.lastCount() + SEP + value.lastId();
    }
}
