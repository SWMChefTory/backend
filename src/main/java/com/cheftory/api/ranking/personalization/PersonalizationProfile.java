package com.cheftory.api.ranking.personalization;

import java.util.List;

/**
 * 개인화 프로필.
 *
 * <p>사용자의 선호도를 반영한 개인화 정보를 담습니다.</p>
 *
 * @param keywordsTop 상위 키워드 목록
 * @param channelsTop 상위 채널 목록
 */
public record PersonalizationProfile(List<String> keywordsTop, List<String> channelsTop) {}
