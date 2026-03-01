package com.cheftory.api._common.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdatedAtIdCursor(LocalDateTime lastUpdatedAt, UUID lastId) {

    public static final UUID MIN_UUID = new UUID(0L, 0L);
    public static final LocalDateTime DEFAULT_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    public static UpdatedAtIdCursor initial() {
        return new UpdatedAtIdCursor(DEFAULT_TIME, MIN_UUID);
    }
}
