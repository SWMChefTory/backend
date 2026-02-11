package com.cheftory.api._common.cursor;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * CountIdCursor 커서를 인코딩/디코딩하는 코덱.
 */
@Component
public class CountIdCursorCodec implements CursorCodec<CountIdCursor> {
    private static final String SEP = "|";

    @Override
    public CountIdCursor decode(String cursor) throws CursorException {

        try {
            int idx = cursor.lastIndexOf(SEP);
            long c = Long.parseLong(cursor.substring(0, idx));
            UUID id = UUID.fromString(cursor.substring(idx + 1));
            return new CountIdCursor(c, id);
        } catch (Exception e) {
            throw new CursorException(CursorErrorCode.INVALID_CURSOR);
        }
    }

    @Override
    public String encode(CountIdCursor value) {
        return value.lastCount() + SEP + value.lastId();
    }
}
