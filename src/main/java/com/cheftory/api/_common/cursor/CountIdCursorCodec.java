package com.cheftory.api._common.cursor;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CountIdCursorCodec implements CursorCodec<CountIdCursor> {
  private static final String SEP = "|";

  @Override
  public CountIdCursor decode(String cursor) {
    int idx = cursor.lastIndexOf(SEP);
    long c = Long.parseLong(cursor.substring(0, idx));
    UUID id = UUID.fromString(cursor.substring(idx + 1));
    return new CountIdCursor(c, id);
  }

  @Override
  public String encode(CountIdCursor value) {
    return value.lastCount() + SEP + value.lastId();
  }
}
