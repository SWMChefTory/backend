package com.cheftory.api._common.cursor;

import org.springframework.stereotype.Component;

@Component
public class RankCursorCodec extends AbstractCursorCodecSupport implements CursorCodec<RankCursor> {
    private static final String SEP = "|";

    @Override
    public RankCursor decode(String cursor) throws CursorException {
        try {
            requireNonBlank(cursor);

            int separatorIndex = cursor.lastIndexOf(SEP);
            requireSeparatorInMiddle(separatorIndex, cursor.length());

            String key = cursor.substring(0, separatorIndex);
            int lastRank = Integer.parseInt(cursor.substring(separatorIndex + 1));
            return new RankCursor(key, lastRank);
        } catch (Exception exception) {
            throw invalidCursor(exception);
        }
    }

    @Override
    public String encode(RankCursor value) {
        return value.rankingKey() + SEP + value.lastRank();
    }
}
