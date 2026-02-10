package com.cheftory.api.ranking;

/**
 * 랭킹 서피스 타입.
 *
 * <p>랭킹이 노출되는 화면/카테고리 유형을 정의합니다.</p>
 */
public enum RankingSurfaceType {
    /** 한식 */
    CUISINE_KOREAN,
    /** 분식 */
    CUISINE_SNACK,
    /** 중식 */
    CUISINE_CHINESE,
    /** 일식 */
    CUISINE_JAPANESE,
    /** 양식 */
    CUISINE_WESTERN,
    /** 디저트 */
    CUISINE_DESSERT,
    /** 건강식 */
    CUISINE_HEALTHY,
    /** 이유식 */
    CUISINE_BABY,
    /** 간편식 */
    CUISINE_SIMPLE;

    /** 메시지 키 접두사 */
    private static final String MESSAGE_KEY_PREFIX = "recipe.cuisine.";
    /** 요리 타입 접두사 */
    private static final String CUISINE_PREFIX = "CUISINE_";

    /**
     * 메시지 키를 반환합니다.
     *
     * @return 메시지 키
     */
    public String messageKey() {
        String name = name();
        String suffix = name.startsWith(CUISINE_PREFIX) ? name.substring(CUISINE_PREFIX.length()) : name;
        return MESSAGE_KEY_PREFIX + suffix.toLowerCase();
    }
}
