package com.cheftory.api.recipe.content.youtubemeta.entity;

/**
 * YouTube 메타 상태 열거형
 *
 * <p>레시피와 연결된 YouTube 영상의 현재 상태를 나타냅니다.</p>
 */
public enum YoutubeMetaStatus {
    /**
     * 활성 상태 - 정상적으로 사용 가능한 영상
     */
    ACTIVE,
    /**
     * 실패 상태 - 메타데이터 추출 실패
     */
    FAILED,
    /**
     * 차단됨 - YouTube 정책 위반으로 차단된 영상
     */
    BANNED,
    /**
     * 제한됨 - 지역 제한 등으로 접근이 제한된 영상
     */
    BLOCKED
}
