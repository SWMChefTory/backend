package com.cheftory.api.ranking.personalization;

import java.util.List;

/**
 * 개인화 집계용 시드 문서.
 *
 * @param keywords 키워드 목록
 * @param channelTitle 채널명
 */
public record RankingPersonalizationSeed(List<String> keywords, String channelTitle) {}
