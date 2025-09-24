package com.cheftory.api.recipeinfo.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeYoutubeMeta Entity")
public class RecipeYoutubeMetaTest {

  @Nested
  @DisplayName("레시피 유튜브 메타데이터 생성")
  class CreateRecipeYoutubeMeta {

    private URI videoUri;
    private String title;
    private String thumbnailUrl;
    private Integer videoSeconds;

    @BeforeEach
    void setUp() {
      videoUri = URI.create("https://www.youtube.com/watch?v=testvideoid");
      title = "Sample Video";
      thumbnailUrl = "https://img.youtube.com/vi/testvideoid/test.jpg";
      videoSeconds = 213;
    }

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {
      private YoutubeVideoInfo youtubeVideoInfo;
      private Clock clock;
      private LocalDateTime now;
      private UUID recipeId;

      @BeforeEach
      void beforeEach() {
        clock = mock(Clock.class);
        youtubeVideoInfo = mock(YoutubeVideoInfo.class);
        now = LocalDateTime.now();
        recipeId = UUID.randomUUID();
        doReturn(videoUri).when(youtubeVideoInfo).getVideoUri();
        doReturn(title).when(youtubeVideoInfo).getTitle();
        doReturn(URI.create(thumbnailUrl)).when(youtubeVideoInfo).getThumbnailUrl();
        doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
        doReturn(now).when(clock).now();
      }

      @Nested
      @DisplayName("When - 유튜브 비디오 메타 정보를 생성한다면")
      class WhenValidParameters {

        private RecipeYoutubeMeta recipeYoutubeMeta;

        @BeforeEach
        void beforeEach() {
          recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
        }

        @Test
        @DisplayName("Then - 레시피 유튜브 메타데이터가 생성된다.")
        void itCreatesRecipeYoutubeMeta() {
          assertThat(recipeYoutubeMeta).isNotNull();
          assertThat(recipeYoutubeMeta.getId()).isNotNull();
          assertThat(recipeYoutubeMeta.getVideoUri()).isEqualTo(videoUri);
          assertThat(recipeYoutubeMeta.getTitle()).isEqualTo(title);
          assertThat(recipeYoutubeMeta.getThumbnailUrl()).isEqualTo(URI.create(thumbnailUrl));
          assertThat(recipeYoutubeMeta.getVideoSeconds()).isEqualTo(videoSeconds);
          assertThat(recipeYoutubeMeta.getCreatedAt()).isEqualTo(now);
          assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.ACTIVE);
          assertThat(recipeYoutubeMeta.getVideoId()).isEqualTo("testvideoid");
          assertThat(recipeYoutubeMeta.getRecipeId()).isEqualTo(recipeId);
        }
      }

      @Nested
      @DisplayName("When - 유튜브 비디오 메타 정보를 차단한다면")
      class WhenBanningYoutubeMeta {

        private RecipeYoutubeMeta recipeYoutubeMeta;

        @BeforeEach
        void beforeEach() {
          recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
          recipeYoutubeMeta.ban();
        }

        @Test
        @DisplayName("Then - 레시피 유튜브 메타데이터가 차단된다.")
        void itBansRecipeYoutubeMeta() {
          assertThat(recipeYoutubeMeta).isNotNull();
          assertTrue(recipeYoutubeMeta.isBanned());
          assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.BANNED);
        }
      }

      @Nested
      @DisplayName("When - 유튜브 비디오 메타 정보가 차단되었는지 확인한다면")
      class WhenCheckingIfBanned {
        private RecipeYoutubeMeta recipeYoutubeMeta;

        @BeforeEach
        void beforeEach() {
          recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
        }

        @Test
        @DisplayName("Then - 레시피 유튜브 메타데이터가 차단되지 않았다.")
        void itIsNotBanned() {
          assertThat(recipeYoutubeMeta).isNotNull();
          assertFalse(recipeYoutubeMeta.isBanned());
          assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.ACTIVE);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 유튜브 메타데이터 차단")
  class BanRecipeYoutubeMeta {

    @Nested
    @DisplayName("Given - 유효한 레시피 유튜브 메타데이터가 주어졌을 때")
    class GivenValidRecipeYoutubeMeta {

      private RecipeYoutubeMeta recipeYoutubeMeta;
      private URI videoUri;
      private String title;
      private String thumbnailUrl;
      private Integer videoSeconds;
      private Clock clock;
      private LocalDateTime now;
      private UUID recipeId;

      @BeforeEach
      void beforeEach() {
        videoUri = URI.create("https://www.youtube.com/watch?v=testvideoid");
        title = "Sample Video";
        thumbnailUrl = "https://img.youtube.com/vi/testvideoid/test.jpg";
        videoSeconds = 213;
        clock = mock(Clock.class);
        YoutubeVideoInfo youtubeVideoInfo = mock(YoutubeVideoInfo.class);
        now = LocalDateTime.now();
        recipeId = UUID.randomUUID();
        doReturn(videoUri).when(youtubeVideoInfo).getVideoUri();
        doReturn(title).when(youtubeVideoInfo).getTitle();
        doReturn(URI.create(thumbnailUrl)).when(youtubeVideoInfo).getThumbnailUrl();
        doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
        doReturn(now).when(clock).now();

        recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
      }

      @Nested
      @DisplayName("When - 유튜브 메타데이터를 차단한다면")
      class WhenBanningYoutubeMeta {

        @BeforeEach
        void beforeEach() {
          recipeYoutubeMeta.ban();
        }

        @Test
        @DisplayName("Then - 레시피 유튜브 메타데이터가 차단된다.")
        void itBansRecipeYoutubeMeta() {
          assertThat(recipeYoutubeMeta).isNotNull();
          assertTrue(recipeYoutubeMeta.isBanned());
          assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.BANNED);
        }
      }
    }
  }
}
