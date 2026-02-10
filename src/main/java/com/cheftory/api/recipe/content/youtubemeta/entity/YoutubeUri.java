package com.cheftory.api.recipe.content.youtubemeta.entity;

import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * YouTube URI 파싱 및 정규화 클래스
 *
 * <p>다양한 형식의 YouTube URL을 파싱하고 표준 형식으로 정규화합니다.</p>
 * <p>지원하는 URL 형식:</p>
 * <ul>
 *   <li>youtube.com/watch?v=VIDEO_ID</li>
 *   <li>youtu.be/VIDEO_ID (공유 URL)</li>
 * </ul>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class YoutubeUri {

    /**
     * 비디오 ID
     */
    private String videoId;
    /**
     * 정규화된 URL
     */
    private URI normalizedUrl;

    /**
     * URI로부터 YoutubeUri 객체 생성
     *
     * <p>YouTube URL을 파싱하여 비디오 ID를 추출하고 정규화된 URL을 생성합니다.</p>
     *
     * @param uri YouTube URI
     * @return 파싱된 YoutubeUri 객체
     * @throws YoutubeMetaException URL 파싱 실패 시
     */
    public static YoutubeUri from(URI uri) throws YoutubeMetaException {
        UriComponents u = UriComponentsBuilder.fromUri(uri).build();
        String id = extractId(u);

        String normalizedPath = "https://www.youtube.com/watch";
        String normalizedQueryKey = "v";

        URI normalized = UriComponentsBuilder.fromUriString(normalizedPath)
                .queryParam(normalizedQueryKey, id)
                .build()
                .toUri();

        return new YoutubeUri(id, normalized);
    }

    /**
     * UriComponents에서 비디오 ID 추출
     *
     * @param url URI 컴포넌트
     * @return 비디오 ID
     * @throws YoutubeMetaException URL 형식이 올바르지 않을 경우
     */
    private static String extractId(UriComponents url) throws YoutubeMetaException {
        String host = url.getHost();
        if (Objects.isNull(host) || host.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_NULL);
        }
        if (host.equals("www.youtu.be") || host.equals("youtu.be")) {
            return extractIdFromSharedUrl(url);
        }

        if (host.equals("www.youtube.com") || host.equals("youtube.com")) {
            return extractIdFromGeneralUrl(url);
        }
        throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID);
    }

    /**
     * youtu.be 공유 URL에서 비디오 ID 추출
     *
     * @param url URI 컴포넌트
     * @return 비디오 ID
     * @throws YoutubeMetaException URL 형식이 올바르지 않을 경우
     */
    private static String extractIdFromSharedUrl(UriComponents url) throws YoutubeMetaException {
        List<String> pathSegments = url.getPathSegments();

        if (pathSegments.isEmpty()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL);
        }

        String videoId = pathSegments.getFirst();
        if (Objects.isNull(videoId) || videoId.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID);
        }

        return videoId;
    }

    /**
     * 일반 youtube.com URL에서 비디오 ID 추출
     *
     * @param url URI 컴포넌트
     * @return 비디오 ID
     * @throws YoutubeMetaException URL 형식이 올바르지 않을 경우
     */
    private static String extractIdFromGeneralUrl(UriComponents url) throws YoutubeMetaException {
        String path = url.getPath();
        if (Objects.isNull(path) || path.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL);
        }
        if (Objects.isNull(url.getQuery())) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }
        if (!path.equals("/watch")) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID);
        }

        List<String> firstQueryValue = url.getQueryParams().get("v");

        if (Objects.isNull(firstQueryValue) || firstQueryValue.isEmpty()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }

        String videoId = firstQueryValue.getFirst();
        if (Objects.isNull(videoId) || videoId.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }

        return videoId;
    }
}
