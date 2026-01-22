package com.cheftory.api._common.cursor;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RankingCursorCodec implements CursorCodec<RankingCursor> {
    private static final String SEP = "|";

    @Override
    public RankingCursor decode(String cursor) {
        int a = cursor.indexOf(SEP);

        UUID requestId = UUID.fromString(cursor.substring(0, a));
        String searchAfter = cursor.substring(a + 1);

        return new RankingCursor(requestId, searchAfter);
    }

    @Override
    public String encode(RankingCursor value) {
        return value.requestId() + SEP + value.searchAfter();
    }
}
