package com.cheftory.api._common.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record ViewedAtCursor(LocalDateTime lastViewedAt, UUID lastId) {}
