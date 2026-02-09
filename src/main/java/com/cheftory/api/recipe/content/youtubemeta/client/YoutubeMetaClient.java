package com.cheftory.api.recipe.content.youtubemeta.client;

import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;

/**
 * 유튜브 메타 정보 외부 클라이언트 인터페이스
 */
public interface YoutubeMetaClient {
    /**
     * 유튜브 비디오 정보 조회
     *
     * @param youtubeUri 유튜브 URI
     * @return 비디오 정보
     * @throws YoutubeMetaException 조회 실패 시
     */
    YoutubeVideoInfo fetch(YoutubeUri youtubeUri) throws YoutubeMetaException;

    /**
     * 유튜브 비디오 차단 여부 확인
     *
     * @param youtubeUri 유튜브 URI
     * @return 차단 여부
     */
    Boolean isBlocked(YoutubeUri youtubeUri);
}
