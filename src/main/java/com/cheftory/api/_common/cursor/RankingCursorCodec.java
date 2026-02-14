package com.cheftory.api._common.cursor;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RankingCursorCodec extends AbstractCursorCodecSupport implements CursorCodec<RankingCursor> {
    private static final String SEP = "|";

    @Override
    public RankingCursor decode(String cursor) throws CursorException {
        try {
            requireNonBlank(cursor);

            int separatorIndex = cursor.indexOf(SEP);
            requireSeparatorInMiddle(separatorIndex, cursor.length());

            UUID requestId = UUID.fromString(cursor.substring(0, separatorIndex));
            String searchAfter = cursor.substring(separatorIndex + 1);

            return new RankingCursor(requestId, searchAfter);
        } catch (Exception exception) {
            throw invalidCursor(exception);
        }
    }

    @Override
    public String encode(RankingCursor value) {
        return value.requestId() + SEP + value.searchAfter();
    }
}
