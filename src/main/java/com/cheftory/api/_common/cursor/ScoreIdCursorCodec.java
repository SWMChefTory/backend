package com.cheftory.api._common.cursor;

import com.cheftory.api._common.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreIdCursorCodec extends AbstractCursorCodecSupport implements CursorCodec<ScoreIdCursor> {
    private static final String SEP = "|";
    private final Clock clock;

    @Override
    public ScoreIdCursor decode(String cursor) throws CursorException {
        try {
            requireNonBlank(cursor);

            int firstSeparator = cursor.indexOf(SEP);
            int secondSeparator = cursor.indexOf(SEP, firstSeparator + 1);
            int lastSeparator = cursor.lastIndexOf(SEP);
            requireSeparatorInMiddle(firstSeparator, cursor.length());
            requireSeparatorInMiddle(lastSeparator, cursor.length());
            if (secondSeparator <= firstSeparator + 1 || lastSeparator <= secondSeparator + 1) throw invalidCursor();

            double score = Double.parseDouble(cursor.substring(0, firstSeparator));
            String id = cursor.substring(firstSeparator + 1, secondSeparator);
            String anchorNowIso = cursor.substring(secondSeparator + 1, lastSeparator);
            String pitId = cursor.substring(lastSeparator + 1);

            return new ScoreIdCursor(score, id, anchorNowIso, pitId);
        } catch (Exception exception) {
            throw invalidCursor(exception);
        }
    }

    @Override
    public String encode(ScoreIdCursor value) {
        return value.score() + SEP + value.id() + SEP + value.anchorNowIso() + SEP + value.pitId();
    }

    public String newAnchorNow() {
        return clock.now().toString();
    }
}
