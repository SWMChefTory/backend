package com.cheftory.api._common.cursor;

import java.util.UUID;

public record CountIdCursor(long lastCount, UUID lastId) {}
