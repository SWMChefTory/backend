package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("YoutubeUri.from")
class YoutubeUriTest {

    private static final String ID = "j7s9VRsrm9o";
    private static final String NORMALIZED = "https://www.youtube.com/watch?v=" + ID;

    @DisplayName("Given - 유효한 유튜브 일반 URL")
    @Nested
    class GivenValidGeneralUrl {

        private final URI general = URI.create("https://www.youtube.com/watch?v=" + ID);

        @DisplayName("When - from 호출")
        @Test
        void thenReturnsNormalizedAndId() {
            YoutubeUri result = YoutubeUri.from(general);

            assertThat(result.getVideoId()).isEqualTo(ID);
            assertThat(result.getNormalizedUrl()).isEqualTo(URI.create(NORMALIZED));
        }
    }

    @DisplayName("Given - 유효한 유튜브 단축 URL (youtu.be)")
    @Nested
    class GivenValidShortUrl {

        private final URI shortUrl = URI.create("https://youtu.be/" + ID);

        @DisplayName("When - from 호출")
        @Test
        void thenReturnsNormalizedAndId() {
            YoutubeUri result = YoutubeUri.from(shortUrl);

            assertThat(result.getVideoId()).isEqualTo(ID);
            assertThat(result.getNormalizedUrl()).isEqualTo(URI.create(NORMALIZED));
        }
    }

    @DisplayName("Given - 추가 파라미터가 있는 유효한 일반 URL")
    @Nested
    class GivenValidGeneralUrlWithExtraParams {

        private final URI withParams =
                URI.create("https://www.youtube.com/watch?v=" + ID + "&t=100s&list=PLrAXtmRdnEQy");

        @DisplayName("When - from 호출")
        @Test
        void thenStripsExtrasAndKeepsOnlyV() {
            YoutubeUri result = YoutubeUri.from(withParams);

            assertThat(result.getVideoId()).isEqualTo(ID);
            assertThat(result.getNormalizedUrl()).isEqualTo(URI.create(NORMALIZED));
        }
    }

    @DisplayName("Given - 유효하지 않은 URL들")
    @Nested
    class GivenInvalidUrls {

        @Test
        @DisplayName("Invalid host -> YOUTUBE_URL_HOST_INVALID")
        void invalidHost() {
            URI invalidHost = URI.create("https://www.invalid.com/watch?v=" + ID);

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(invalidHost));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID.getErrorCode());
            assertThat(ex.getError().getMessage())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID.getMessage());
        }

        @Test
        @DisplayName("Invalid path (/watch 아님) -> YOUTUBE_URL_PATH_INVALID")
        void invalidPath() {
            URI invalidPath = URI.create("https://www.youtube.com/invalid?v=" + ID);

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(invalidPath));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID.getErrorCode());
            assertThat(ex.getError().getMessage())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID.getMessage());
        }

        @Test
        @DisplayName("쿼리 없음 -> YOUTUBE_URL_QUERY_PARAM_INVALID")
        void nullQuery() {
            URI nullQuery = URI.create("https://www.youtube.com/watch");

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(nullQuery));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
            assertThat(ex.getError().getMessage())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getMessage());
        }

        @Test
        @DisplayName("호스트 없음 -> YOUTUBE_URL_HOST_NULL")
        void nullHost() {
            // scheme만 있고 authority 없음 → host가 null로 파싱됨
            URI nullHost = URI.create("https:///watch?v=" + ID);

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(nullHost));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_NULL.getErrorCode());
            assertThat(ex.getError().getMessage()).isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_NULL.getMessage());
        }

        @Test
        @DisplayName("경로 없음 -> YOUTUBE_URL_PATH_NULL")
        void nullPath() {
            URI nullPath = URI.create("https://www.youtube.com?v=" + ID);

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(nullPath));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL.getErrorCode());
            assertThat(ex.getError().getMessage()).isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL.getMessage());
        }

        @Test
        @DisplayName("v 파라미터 없음 -> YOUTUBE_URL_QUERY_PARAM_INVALID")
        void missingVParam() {
            URI missingV = URI.create("https://www.youtube.com/watch?list=PLrAXtmRdnEQy");

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(missingV));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
            assertThat(ex.getError().getMessage())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getMessage());
        }

        @Test
        @DisplayName("v 파라미터 빈 값 -> YOUTUBE_URL_QUERY_PARAM_INVALID")
        void emptyVParam() {
            URI emptyV = URI.create("https://www.youtube.com/watch?v=");

            YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(emptyV));

            assertThat(ex.getError().getErrorCode())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
            assertThat(ex.getError().getMessage())
                    .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getMessage());
        }
    }
}
