package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("YoutubeUri 엔티티")
class YoutubeUriTest {

    private static final String ID = "j7s9VRsrm9o";
    private static final String NORMALIZED = "https://www.youtube.com/watch?v=" + ID;

    @Nested
    @DisplayName("URL 파싱 (from)")
    class From {

        @Nested
        @DisplayName("Given - 유효한 일반 URL이 주어졌을 때")
        class GivenGeneralUrl {
            URI url;

            @BeforeEach
            void setUp() {
                url = URI.create("https://www.youtube.com/watch?v=" + ID);
            }

            @Nested
            @DisplayName("When - 파싱을 요청하면")
            class WhenParsing {
                YoutubeUri result;

                @BeforeEach
                void setUp() throws YoutubeMetaException {
                    result = YoutubeUri.from(url);
                }

                @Test
                @DisplayName("Then - 정규화된 URL과 ID를 반환한다")
                void thenReturnsNormalized() {
                    assertThat(result.getVideoId()).isEqualTo(ID);
                    assertThat(result.getNormalizedUrl()).isEqualTo(URI.create(NORMALIZED));
                }
            }
        }

        @Nested
        @DisplayName("Given - 유효한 단축 URL이 주어졌을 때")
        class GivenShortUrl {
            URI url;

            @BeforeEach
            void setUp() {
                url = URI.create("https://youtu.be/" + ID);
            }

            @Nested
            @DisplayName("When - 파싱을 요청하면")
            class WhenParsing {
                YoutubeUri result;

                @BeforeEach
                void setUp() throws YoutubeMetaException {
                    result = YoutubeUri.from(url);
                }

                @Test
                @DisplayName("Then - 정규화된 URL과 ID를 반환한다")
                void thenReturnsNormalized() {
                    assertThat(result.getVideoId()).isEqualTo(ID);
                    assertThat(result.getNormalizedUrl()).isEqualTo(URI.create(NORMALIZED));
                }
            }
        }

        @Nested
        @DisplayName("Given - 추가 파라미터가 있는 URL이 주어졌을 때")
        class GivenUrlWithParams {
            URI url;

            @BeforeEach
            void setUp() {
                url = URI.create("https://www.youtube.com/watch?v=" + ID + "&t=100s&list=PLrAXtmRdnEQy");
            }

            @Nested
            @DisplayName("When - 파싱을 요청하면")
            class WhenParsing {
                YoutubeUri result;

                @BeforeEach
                void setUp() throws YoutubeMetaException {
                    result = YoutubeUri.from(url);
                }

                @Test
                @DisplayName("Then - 불필요한 파라미터를 제거하고 반환한다")
                void thenStripsParams() {
                    assertThat(result.getVideoId()).isEqualTo(ID);
                    assertThat(result.getNormalizedUrl()).isEqualTo(URI.create(NORMALIZED));
                }
            }
        }

        @Nested
        @DisplayName("Given - 유효하지 않은 URL이 주어졌을 때")
        class GivenInvalidUrl {

            @Test
            @DisplayName("Then - 잘못된 호스트면 HOST_INVALID 예외를 던진다")
            void invalidHost() {
                URI url = URI.create("https://www.invalid.com/watch?v=" + ID);
                YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(url));
                assertThat(ex.getError().getErrorCode())
                        .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID.getErrorCode());
            }

            @Test
            @DisplayName("Then - 잘못된 경로면 PATH_INVALID 예외를 던진다")
            void invalidPath() {
                URI url = URI.create("https://www.youtube.com/invalid?v=" + ID);
                YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(url));
                assertThat(ex.getError().getErrorCode())
                        .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID.getErrorCode());
            }

            @Test
            @DisplayName("Then - 쿼리가 없으면 QUERY_PARAM_INVALID 예외를 던진다")
            void nullQuery() {
                URI url = URI.create("https://www.youtube.com/watch");
                YoutubeMetaException ex = assertThrows(YoutubeMetaException.class, () -> YoutubeUri.from(url));
                assertThat(ex.getError().getErrorCode())
                        .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
            }
        }
    }
}
