package com.cheftory.api.tracking.entity;

/**
 * 프론트엔드에서 레시피 카드가 노출되는 위치.
 *
 * <p>프론트엔드 SurfaceType과 1:1 매핑됩니다.</p>
 */
public enum SurfaceType {
    /** 홈 > 내 레시피 섹션 */
    HOME_MY_RECIPES,
    /** 홈 > 인기 레시피 섹션 */
    HOME_POPULAR_RECIPES,
    /** 홈 > 인기 숏츠 섹션 */
    HOME_POPULAR_SHORTS,
    /** /user/recipes 전체 목록 */
    USER_RECIPES,
    /** /popular-recipe 인기 레시피 */
    POPULAR_RECIPES,
    /** /search-recipe 트렌딩 */
    SEARCH_TRENDING,
    /** /search-results 검색 결과 */
    SEARCH_RESULTS,
    /** /recommend 카테고리 결과 */
    CATEGORY_RESULTS
}
