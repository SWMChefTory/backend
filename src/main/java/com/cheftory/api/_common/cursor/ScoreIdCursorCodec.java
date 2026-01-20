package com.cheftory.api._common.cursor;

import com.cheftory.api._common.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreIdCursorCodec implements CursorCodec<ScoreIdCursor> {
    private static final String SEP = "|";
    private final Clock clock;

    public ScoreIdCursor decode(String cursor) {
        int a = cursor.indexOf(SEP);
        int b = cursor.indexOf(SEP, a + 1);
        int c = cursor.lastIndexOf(SEP);

        double score = Double.parseDouble(cursor.substring(0, a));
        String id = cursor.substring(a + 1, b);
        String anchorNowIso = cursor.substring(b + 1, c);
        String pitId = cursor.substring(c + 1);

        return new ScoreIdCursor(score, id, anchorNowIso, pitId);
    }

    @Override
    public String encode(ScoreIdCursor value) {
        return value.score() + SEP + value.id() + SEP + value.anchorNowIso() + SEP + value.pitId();
    }

    public String newAnchorNow() {
        return clock.now().toString();
    }
}
