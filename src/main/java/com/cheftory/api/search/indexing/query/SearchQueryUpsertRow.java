package com.cheftory.api.search.indexing.query;

import java.time.LocalDateTime;
import java.util.List;

public record SearchQueryUpsertRow(
        String id,
        String market,
        String title,
        String channelTitle,
        String scope,
        String servingsText,
        List<String> ingredients,
        List<String> keywords,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
