package com.cheftory.api._common.cursor;

import java.util.UUID;

/**
 * 카운트와 ID를 포함하는 커서 레코드.
 *
 * @param lastCount 마지막 카운트
 * @param lastId 마지막 ID
 */
public record CountIdCursor(long lastCount, UUID lastId) {}
