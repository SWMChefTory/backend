package com.cheftory.api._common.cursor;

import java.util.UUID;

public record RankingCursor(UUID requestId, String searchAfter) {}
