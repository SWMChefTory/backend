package com.cheftory.api.search.query;

import java.util.List;
import java.util.UUID;

public record SearchPage(List<UUID> items, String nextCursor) {}
