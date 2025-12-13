package com.cheftory.api.recipeinfo.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.youtubemeta.client.VideoInfoClient;
import com.cheftory.api.recipeinfo.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipeinfo.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeYoutubeMetaService")
public class RecipeYoutubeMetaServiceTest {

  private RecipeYoutubeMetaService recipeYoutubeMetaService;
  private RecipeYoutubeMetaRepository recipeYoutubeMetaRepository;
  private VideoInfoClient videoInfoClient;
  private Clock clock;

  @BeforeEach
  public void setUp() {
    recipeYoutubeMetaRepository = mock(RecipeYoutubeMetaRepository.class);
    videoInfoClient = mock(VideoInfoClient.class);
    clock = mock(Clock.class);
    recipeYoutubeMetaService =
        new RecipeYoutubeMetaService(recipeYoutubeMetaRepository, videoInfoClient, clock);
  }

  @Nested
  @DisplayName("유튜브 메타 차단")
  class BlockYoutubeMeta {

    private UUID recipeId;
    private RecipeYoutubeMeta meta;

    @BeforeEach
    void setUp() {
      recipeId = java.util.UUID.randomUUID();
      meta = mock(RecipeYoutubeMeta.class);
    }

    @Nested
    @DisplayName("Given - 해당 레시피의 메타가 존재하고, 영상이 차단된 경우")
    class GivenMetaExistsAndVideoBlocked {

      @BeforeEach
      void setUp() {
        doReturn(java.util.Optional.of(meta))
            .when(recipeYoutubeMetaRepository)
            .findByRecipeId(recipeId);
        doReturn(java.net.URI.create("https://www.youtube.com/watch?v=blocked"))
            .when(meta)
            .getVideoUri();
        doReturn(true).when(videoInfoClient).isBlockedVideo(any(YoutubeUri.class));
      }

      @Test
      @DisplayName("Then - 메타가 BLOCKED 처리되고 저장된다")
      void thenBlockAndSave() {
        recipeYoutubeMetaService.block(recipeId);

        verify(videoInfoClient).isBlockedVideo(any(YoutubeUri.class));
        verify(meta).block();
        verify(recipeYoutubeMetaRepository).save(meta);
      }
    }

    @Nested
    @DisplayName("Given - 해당 레시피의 메타가 존재하지만, 영상이 차단되지 않은 경우")
    class GivenMetaExistsAndVideoNotBlocked {

      @BeforeEach
      void setUp() {
        doReturn(java.util.Optional.of(meta))
            .when(recipeYoutubeMetaRepository)
            .findByRecipeId(recipeId);
        doReturn(java.net.URI.create("https://www.youtube.com/watch?v=notblocked"))
            .when(meta)
            .getVideoUri();
        doReturn(false).when(videoInfoClient).isBlockedVideo(any(YoutubeUri.class));
      }

      @Test
      @DisplayName("Then - YOUTUBE_META_NOT_BLOCKED_VIDEO 예외가 발생한다")
      void thenThrowNotBlockedVideo() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> recipeYoutubeMetaService.block(recipeId))
            .isInstanceOf(YoutubeMetaException.class)
            .hasFieldOrPropertyWithValue(
                "errorMessage", YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO);

        verify(recipeYoutubeMetaRepository, never()).save(any());
      }
    }

    @Nested
    @DisplayName("Given - 해당 레시피의 메타가 존재하지 않는 경우")
    class GivenMetaNotFound {

      @BeforeEach
      void setUp() {
        doReturn(java.util.Optional.empty())
            .when(recipeYoutubeMetaRepository)
            .findByRecipeId(recipeId);
      }

      @Test
      @DisplayName("Then - YOUTUBE_META_NOT_FOUND 예외가 발생한다")
      void thenThrowNotFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> recipeYoutubeMetaService.block(recipeId))
            .isInstanceOf(YoutubeMetaException.class)
            .hasFieldOrPropertyWithValue(
                "errorMessage", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
      }
    }
  }

  @Nested
  @DisplayName("유튜브 메타 정보 생성")
  class CreateYoutubeMeta {

    private String title;
    private Integer videoSeconds;
    private URI youtubeThumbnailUrl;
    private YoutubeUri youtubeUri = mock(YoutubeUri.class);

    @BeforeEach
    void setUp() {
      title = "Sample Video";
      youtubeThumbnailUrl =
          UriComponentsBuilder.fromUriString("https://img.youtube.com/vi/test/default.jpg")
              .build()
              .toUri();
      videoSeconds = 213;
    }

    @Nested
    @DisplayName("Given - 유효한 YoutubeVideoInfo가 주어졌을 때")
    class GivenValidYoutubeVideoInfo {

      private YoutubeVideoInfo youtubeVideoInfo;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        youtubeVideoInfo =
            YoutubeVideoInfo.from(
                youtubeUri,
                title,
                youtubeThumbnailUrl,
                videoSeconds,
                YoutubeMetaType.NORMAL);
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - create 메서드를 호출하면")
      class WhenCreateYoutubeVideoInfo {

        private RecipeYoutubeMeta recipeYoutubeMeta;

        @BeforeEach
        void setUp() {
          recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
          doReturn(recipeYoutubeMeta)
              .when(recipeYoutubeMetaRepository)
              .save(any(RecipeYoutubeMeta.class));
        }

        @Test
        @DisplayName("Then - RecipeYoutubeMetaRepository의 save 메서드가 호출된다.")
        void thenRecipeYoutubeMetaRepositorySaveCalled() {
          recipeYoutubeMetaService.create(youtubeVideoInfo, recipeId);

          verify(recipeYoutubeMetaRepository).save(any(RecipeYoutubeMeta.class));
        }
      }
    }
  }

  @Nested
  @DisplayName("유튜브 메타 정보 URL로 조회")
  class GetYoutubeMetaByUrl {

    private URI originalUri;
    private UriComponents normalizedUri;
    private RecipeYoutubeMeta meta1;
    private RecipeYoutubeMeta meta2;
    private YoutubeUri youtubeUri;

    @BeforeEach
    void setUp() {
      originalUri = URI.create("https://www.youtube.com/watch?v=test");
      normalizedUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=test").build();
      meta1 = mock(RecipeYoutubeMeta.class);
      meta2 = mock(RecipeYoutubeMeta.class);
      youtubeUri = mock(YoutubeUri.class);
    }

    @Nested
    @DisplayName("Given - 정상적인 URL이 주어졌을 때")
    class GivenValidUrl {

      @BeforeEach
      void setUp() {}

      @Test
      @DisplayName("Then - 해당 URL에 매핑된 RecipeYoutubeMeta 리스트를 반환한다.")
      void thenReturnRecipeYoutubeMetaList() {
        doReturn(java.util.List.of(meta1, meta2))
            .when(recipeYoutubeMetaRepository)
            .findAllByVideoUri(normalizedUri.toUri());

        var result = recipeYoutubeMetaService.getByUrl(originalUri);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(meta1, meta2);
      }

      @Test
      @DisplayName("Then - 결과가 없으면 빈 리스트를 반환한다.")
      void thenReturnEmptyListIfNone() {
        doReturn(java.util.List.of())
            .when(recipeYoutubeMetaRepository)
            .findAllByVideoUri(any(URI.class));

        var result = recipeYoutubeMetaService.getByUrl(originalUri);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
      }

      @Test
      @DisplayName("Then - 결과 중 banned가 하나라도 있으면 RECIPE_BANNED 예외를 던진다.")
      void thenThrowRecipeBannedIfAnyBanned() {
        doReturn(java.util.List.of(meta1, meta2))
            .when(recipeYoutubeMetaRepository)
            .findAllByVideoUri(any(URI.class));
        doReturn(false).when(meta1).isBanned();
        doReturn(true).when(meta2).isBanned();

        assertThatThrownBy(() -> recipeYoutubeMetaService.getByUrl(originalUri))
            .isInstanceOf(YoutubeMetaException.class)
            .hasFieldOrPropertyWithValue("errorMessage", YoutubeMetaErrorCode.YOUTUBE_META_BANNED);
      }

      @Test
      @DisplayName("Then - 모든 메타데이터가 banned가 아니면 정상적으로 리스트를 반환한다")
      void thenReturnListWhenNoneBanned() {
        doReturn(java.util.List.of(meta1, meta2))
            .when(recipeYoutubeMetaRepository)
            .findAllByVideoUri(normalizedUri.toUri());
        doReturn(false).when(meta1).isBanned();
        doReturn(false).when(meta2).isBanned();
        doReturn(false).when(meta1).isBlocked();
        doReturn(false).when(meta2).isBlocked();

        var result = recipeYoutubeMetaService.getByUrl(originalUri);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(meta1, meta2);
      }

      @Test
      @DisplayName("Then - 결과 중 blocked가 하나라도 있으면 YOUTUBE_META_BLOCKED 예외를 던진다.")
      void thenThrowYoutubeMetaBlockedIfAnyBlocked() {
        doReturn(java.util.List.of(meta1, meta2))
            .when(recipeYoutubeMetaRepository)
            .findAllByVideoUri(any(URI.class));
        doReturn(false).when(meta1).isBanned();
        doReturn(false).when(meta2).isBanned();
        doReturn(false).when(meta1).isBlocked();
        doReturn(true).when(meta2).isBlocked();

        assertThatThrownBy(() -> recipeYoutubeMetaService.getByUrl(originalUri))
            .isInstanceOf(YoutubeMetaException.class)
            .hasFieldOrPropertyWithValue("errorMessage", YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED);
      }

      @Test
      @DisplayName("Then - 모든 메타데이터가 blocked가 아니고 banned가 아니면 정상적으로 리스트를 반환한다")
      void thenReturnListWhenNoneBannedAndNoneBlocked() {
        doReturn(java.util.List.of(meta1, meta2))
            .when(recipeYoutubeMetaRepository)
            .findAllByVideoUri(normalizedUri.toUri());
        doReturn(false).when(meta1).isBanned();
        doReturn(false).when(meta2).isBanned();
        doReturn(false).when(meta1).isBlocked();
        doReturn(false).when(meta2).isBlocked();

        var result = recipeYoutubeMetaService.getByUrl(originalUri);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(meta1, meta2);
      }
    }
  }

  @Nested
  @DisplayName("유튜브 영상 정보 URL로 조회")
  class GetVideoInfoByUrl {

    private URI originalUri;
    private YoutubeVideoInfo youtubeVideoInfo;

    @BeforeEach
    void setUp() {
      originalUri = URI.create("https://www.youtube.com/watch?v=test");
      youtubeVideoInfo = mock(YoutubeVideoInfo.class);
    }

    @Nested
    @DisplayName("Given - 정상적인 URL이 주어졌을 때")
    class GivenValidUrl {

      @BeforeEach
      void setUp() {
        doReturn(youtubeVideoInfo).when(videoInfoClient).fetchVideoInfo(any(YoutubeUri.class));
      }

      @Nested
      @DisplayName("When - getVideoInfo 메서드를 호출하면")
      class WhenGetVideoInfo {

        @Test
        @DisplayName("Then - 해당 URL에 매핑된 YoutubeVideoInfo를 반환한다.")
        void thenReturnYoutubeVideoInfo() {
          YoutubeVideoInfo result = recipeYoutubeMetaService.getVideoInfo(originalUri);

          assertThat(result).isNotNull();
          assertThat(result).isEqualTo(youtubeVideoInfo);
        }
      }

      @Nested
      @DisplayName("Given - VideoInfoClient에서 예외가 발생할 때")
      class GivenVideoInfoClientThrowsException {

        @BeforeEach
        void setUp() {
          doReturn(null).when(videoInfoClient).fetchVideoInfo(any(YoutubeUri.class));
        }

        @Nested
        @DisplayName("When - getVideoInfo 메서드를 호출하면")
        class WhenGetVideoInfo {

          @Test
          @DisplayName("Then - null을 반환한다")
          void thenReturnNull() {
            YoutubeVideoInfo result = recipeYoutubeMetaService.getVideoInfo(originalUri);

            assertThat(result).isNull();
          }
        }
      }
    }

    @Nested
    @DisplayName("유튜브 메타 정보 조회")
    class FindYoutubeMeta {

      private UUID recipeId;
      private RecipeYoutubeMeta recipeYoutubeMeta;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipeYoutubeMeta = mock(RecipeYoutubeMeta.class);
      }

      @Nested
      @DisplayName("Given - 존재하는 Recipe ID가 주어졌을 때")
      class GivenExistingRecipeId {

        @BeforeEach
        void setUp() {
          doReturn(java.util.Optional.of(recipeYoutubeMeta))
              .when(recipeYoutubeMetaRepository)
              .findByRecipeId(recipeId);
        }

        @Test
        @DisplayName("Then - 해당 Recipe ID에 매핑된 RecipeYoutubeMeta를 반환한다.")
        void thenReturnRecipeYoutubeMeta() {
          RecipeYoutubeMeta result = recipeYoutubeMetaService.get(recipeId);

          assertThat(result).isNotNull();
          assertThat(result).isEqualTo(recipeYoutubeMeta);
        }
      }

      @Nested
      @DisplayName("Given - 존재하지 않는 Recipe ID가 주어졌을 때")
      class GivenNonExistentRecipeId {

        @BeforeEach
        void setUp() {
          doReturn(java.util.Optional.empty())
              .when(recipeYoutubeMetaRepository)
              .findByRecipeId(recipeId);
        }

        @Test
        @DisplayName("Then - YoutubeMetaException 예외가 발생한다.")
        void thenThrowYoutubeMetaException() {
          assertThatThrownBy(() -> recipeYoutubeMetaService.get(recipeId))
              .isInstanceOf(YoutubeMetaException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
        }
      }
    }

    @Nested
    @DisplayName("유튜브 메타 정보 다건 조회")
    class FindInYoutubeMeta {

      private UUID recipeId;
      private RecipeYoutubeMeta recipeYoutubeMeta;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipeYoutubeMeta = mock(RecipeYoutubeMeta.class);
      }

      @Nested
      @DisplayName("Given - 존재하는 Recipe ID 리스트가 주어졌을 때")
      class GivenExistingRecipeIdList {

        @BeforeEach
        void setUp() {
          doReturn(java.util.List.of(recipeYoutubeMeta))
              .when(recipeYoutubeMetaRepository)
              .findAllByRecipeIdIn(any());
        }

        @Nested
        @DisplayName("When - findsByRecipes 메서드를 호출하면")
        class WhenFindsByRecipes {

          @Test
          @DisplayName("Then - 해당 리스트에 매핑된 RecipeYoutubeMeta 리스트를 반환한다")
          void thenReturnRecipeYoutubeMetaList() {
            var result = recipeYoutubeMetaService.getByRecipes(java.util.List.of(recipeId));

            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result).contains(recipeYoutubeMeta);
          }
        }
      }

      @Nested
      @DisplayName("Given - 빈 Recipe ID 리스트가 주어졌을 때")
      class GivenEmptyRecipeIdList {

        @BeforeEach
        void setUp() {
          doReturn(java.util.List.of())
              .when(recipeYoutubeMetaRepository)
              .findAllByRecipeIdIn(any());
        }

        @Nested
        @DisplayName("When - findsByRecipes 메서드를 호출하면")
        class WhenFindsByRecipes {

          @Test
          @DisplayName("Then - 빈 리스트를 반환한다")
          void thenReturnEmptyList() {
            var result = recipeYoutubeMetaService.getByRecipes(java.util.List.of());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
          }
        }
      }
    }

    @Nested
    @DisplayName("유튜브 메타 정보 차단")
    class BanYoutubeMeta {

      @Nested
      @DisplayName("Given - 존재하는 Recipe ID가 주어졌을 때")
      class GivenExistingRecipeId {

        private RecipeYoutubeMeta recipeYoutubeMeta;
        private UUID recipeId;

        @BeforeEach
        void setUp() {
          recipeYoutubeMeta = mock(RecipeYoutubeMeta.class);
          recipeId = UUID.randomUUID();
          doReturn(Optional.of(recipeYoutubeMeta))
              .when(recipeYoutubeMetaRepository)
              .findByRecipeId(recipeId);
        }

        @Nested
        @DisplayName("When - ban 메서드를 호출하면")
        class WhenBanMethod {

          @Test
          @DisplayName("Then - 해당 RecipeYoutubeMeta의 ban 메서드가 호출되고 저장된다")
          void thenCallBanMethodAndSave() {
            recipeYoutubeMetaService.ban(recipeId);

            verify(recipeYoutubeMeta).ban();
            verify(recipeYoutubeMetaRepository).save(recipeYoutubeMeta);
          }
        }
      }

      @Nested
      @DisplayName("Given - 존재하지 않는 Recipe ID가 주어졌을 때")
      class GivenNonExistentRecipeId {

        private UUID recipeId;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          doReturn(Optional.empty()).when(recipeYoutubeMetaRepository).findByRecipeId(recipeId);
        }

        @Nested
        @DisplayName("When - ban 메서드를 호출하면")
        class WhenBanMethod {

          @Test
          @DisplayName("Then - YoutubeMetaException 예외가 발생한다")
          void thenThrowYoutubeMetaException() {
            assertThatThrownBy(() -> recipeYoutubeMetaService.ban(recipeId))
                .isInstanceOf(YoutubeMetaException.class)
                .hasFieldOrPropertyWithValue(
                    "errorMessage", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
          }
        }
      }
    }
  }
}
