package com.cheftory.api._common.cursor;

import org.springframework.stereotype.Component;

@Component
public class RankCursorCodec implements CursorCodec<RankCursor> {
  private static final String SEP = "|";

  public RankCursor decode(String cursor) {
    int idx = cursor.lastIndexOf(SEP);

    String key = cursor.substring(0, idx);
    int lastRank = Integer.parseInt(cursor.substring(idx + 1));
    return new RankCursor(key, lastRank);
  }

  @Override
  public String encode(RankCursor value) {
    return value.rankingKey() + SEP + value.lastRank();
  }
}
