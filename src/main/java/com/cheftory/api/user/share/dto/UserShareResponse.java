package com.cheftory.api.user.share.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 유저 공유 응답 DTO
 *
 * <p>유저의 공유 횟수 정보를 응답으로 전달하기 위한 DTO입니다.</p>
 */
public record UserShareResponse(
        /**
         * 공유 횟수
         */
        @JsonProperty("share_count") int shareCount) {

    /**
     * 공유 횟수로부터 UserShareResponse 생성
     *
     * @param shareCount 공유 횟수
     * @return 유저 공유 응답 DTO
     */
    public static UserShareResponse of(int shareCount) {
        return new UserShareResponse(shareCount);
    }
}
