package com.cheftory.api.ranking.personalization;

import java.util.List;

public record PersonalizationProfile(List<String> keywordsTop, List<String> channelsTop) {}
