package com.cheftory.api.recipeinfo.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("YoutubeUrlNormalizer")
public class YoutubeUrlNormalizerTest {

  private YoutubeUrlNormalizer urlNormalizer;

  @BeforeEach
  void setUp() {
    urlNormalizer = new YoutubeUrlNormalizer();
  }

  @DisplayName("Given - 유효한 유튜브 일반 URI이 주어졌을 때")
  @Nested
  class GivenValidYoutubeGeneralUrl {

    UriComponents youTubeUri;

    @BeforeEach
    void setUp() {
      // 백종원 유튜브 영상
      youTubeUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=j7s9VRsrm9o").build();
    }

    @DisplayName("When - normalize 메서드를 호출하면")
    @Nested
    class WhenCallNormalize {

      UriComponents normalizedUri;

      @BeforeEach
      void setUp() {
        normalizedUri = urlNormalizer.normalize(youTubeUri);
      }

      @DisplayName("Then - 정상적으로 정규화된 URI를 반환한다")
      @Test
      void ThenReturnNormalizedUri() {
        UriComponents expectedUri =
            UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=j7s9VRsrm9o")
                .build();

        assertThat(normalizedUri.toUri()).isEqualTo(expectedUri.toUri());
      }
    }
  }

  @DisplayName("Given - 유효한 유튜브 단축 URI이 주어졌을 때")
  @Nested
  class GivenValidYoutubeShortUrl {

    UriComponents youTubeShortUri;

    @BeforeEach
    void setUp() {
      youTubeShortUri = UriComponentsBuilder.fromUriString("https://youtu.be/j7s9VRsrm9o").build();
    }

    @DisplayName("When - normalize 메서드를 호출하면")
    @Nested
    class WhenCallNormalize {

      UriComponents normalizedUri;

      @BeforeEach
      void setUp() {
        normalizedUri = urlNormalizer.normalize(youTubeShortUri);
      }

      @DisplayName("Then - 정상적으로 정규화된 URI를 반환한다")
      @Test
      void ThenReturnNormalizedUri() {
        UriComponents expectedUri =
            UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=j7s9VRsrm9o")
                .build();

        assertThat(normalizedUri.toUri()).isEqualTo(expectedUri.toUri());
      }
    }
  }

  @DisplayName("Given - 추가 파라미터가 있는 유효한 유튜브 URI이 주어졌을 때")
  @Nested
  class GivenValidYoutubeUrlWithExtraParams {

    UriComponents youTubeUriWithParams;

    @BeforeEach
    void setUp() {
      youTubeUriWithParams =
          UriComponentsBuilder.fromUriString(
                  "https://www.youtube.com/watch?v=j7s9VRsrm9o&t=100s&list=PLrAXtmRdnEQy")
              .build();
    }

    @DisplayName("When - normalize 메서드를 호출하면")
    @Nested
    class WhenCallNormalize {

      UriComponents normalizedUri;

      @BeforeEach
      void setUp() {
        normalizedUri = urlNormalizer.normalize(youTubeUriWithParams);
      }

      @DisplayName("Then - 정상적으로 정규화된 URI를 반환한다")
      @Test
      void ThenReturnNormalizedUri() {
        UriComponents expectedUri =
            UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=j7s9VRsrm9o")
                .build();

        assertThat(normalizedUri.toUri()).isEqualTo(expectedUri.toUri());
      }
    }
  }

  @DisplayName("Given - 유효하지 않은 유튜브 URI이 주어졌을 때")
  @Nested
  class GivenInvalidYoutubeUrl {

    private UriComponents invalidHostUri;
    private UriComponents invalidPathUri;
    private UriComponents nullQueryUri;
    private UriComponents nullHostUri;
    private UriComponents nullPathUri;
    private UriComponents missingVParamUri;
    private UriComponents emptyVParamUri;

    @BeforeEach
    void setUp() {
      invalidHostUri =
          UriComponentsBuilder.fromUriString("https://www.invalid.com/watch?v=j7s9VRsrm9o").build();
      invalidPathUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/invalid?v=j7s9VRsrm9o")
              .build();
      nullQueryUri = UriComponentsBuilder.fromUriString("https://www.youtube.com/watch").build();
      nullHostUri = UriComponentsBuilder.fromUriString("https:///watch?v=j7s9VRsrm9o").build();
      nullPathUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com?v=j7s9VRsrm9o").build();
      missingVParamUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?list=PLrAXtmRdnEQy")
              .build();
      emptyVParamUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=").build();
    }

    @DisplayName("When - normalize 메서드를 호출하면")
    @Nested
    class WhenCallNormalize {

      @DisplayName("Then - 유효하지 않은 호스트일 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForInvalidHost() {
        YoutubeMetaException exception =
            assertThrows(YoutubeMetaException.class, () -> urlNormalizer.normalize(invalidHostUri));

        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID.getMessage());
      }

      @DisplayName("Then - 유효하지 않은 경로일 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForInvalidPath() {
        YoutubeMetaException exception =
            assertThrows(YoutubeMetaException.class, () -> urlNormalizer.normalize(invalidPathUri));
        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID.getMessage());
      }

      @DisplayName("Then - 쿼리가 null일 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForNullQuery() {
        YoutubeMetaException exception =
            assertThrows(YoutubeMetaException.class, () -> urlNormalizer.normalize(nullQueryUri));
        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getMessage());
      }

      @DisplayName("Then - 호스트가 null일 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForNullHost() {
        YoutubeMetaException exception =
            assertThrows(YoutubeMetaException.class, () -> urlNormalizer.normalize(nullHostUri));
        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_NULL.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_NULL.getMessage());
      }

      @DisplayName("Then - 경로가 null일 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForNullPath() {
        YoutubeMetaException exception =
            assertThrows(YoutubeMetaException.class, () -> urlNormalizer.normalize(nullPathUri));
        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL.getMessage());
      }

      @DisplayName("Then - v 파라미터가 없을 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForMissingVParam() {
        YoutubeMetaException exception =
            assertThrows(
                YoutubeMetaException.class, () -> urlNormalizer.normalize(missingVParamUri));
        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getMessage());
      }

      @DisplayName("Then - v 파라미터가 빈 값일 때 YoutubeMetaException 예외가 발생한다")
      @Test
      void ThenThrowYoutubeMetaExceptionForEmptyVParam() {
        YoutubeMetaException exception =
            assertThrows(YoutubeMetaException.class, () -> urlNormalizer.normalize(emptyVParamUri));
        assertThat(exception.getErrorMessage().getErrorCode())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getErrorCode());
        assertThat(exception.getErrorMessage().getMessage())
            .isEqualTo(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID.getMessage());
      }
    }
  }
}
