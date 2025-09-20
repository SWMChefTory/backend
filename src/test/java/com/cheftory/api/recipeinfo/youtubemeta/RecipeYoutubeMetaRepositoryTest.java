package com.cheftory.api.recipeinfo.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeYoutubeMetaRepository")
public class RecipeYoutubeMetaRepositoryTest extends DbContextTest {

  @Autowired private RecipeYoutubeMetaRepository repository;

  @MockitoBean private Clock clock;

  @Nested
  @DisplayName("레시피 유튜브 메타데이터 저장")
  class SaveRecipeYoutubeMeta {

    @Nested
    @DisplayName("Given - 유효한 레시피 유튜브 메타데이터가 주어졌을 때")
    class GivenValidRecipeYoutubeMeta {

      private UriComponents youtubeUri;
      private String title;
      private Integer videoSeconds;
      private URI youtubeThumbnailUrl;

      @BeforeEach
      void setUp() {
        youtubeUri =
            UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=j7s9VRsrm9o")
                .build();
        title = "백종원의 요리비책 - 김치볶음밥";
        videoSeconds = 300;
        youtubeThumbnailUrl =
            URI.create("https://img.youtube.com/vi/j7s9VRsrm9o/maxresdefault.jpg");
      }

      @DisplayName("When - 레시피 유튜브 메타데이터를 저장한다면")
      @Nested
      class WhenSavingRecipeYoutubeMeta {

        private YoutubeVideoInfo youtubeVideoInfo;
        private RecipeYoutubeMeta recipeYoutubeMeta;
        private UUID recipeId;
        private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        @BeforeEach
        void beforeEach() {
          youtubeVideoInfo = mock(YoutubeVideoInfo.class);
          recipeId = UUID.randomUUID();
          doReturn(youtubeThumbnailUrl).when(youtubeVideoInfo).getThumbnailUrl();
          doReturn(title).when(youtubeVideoInfo).getTitle();
          doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
          doReturn(youtubeUri.toUri()).when(youtubeVideoInfo).getVideoUri();
          doReturn(FIXED_TIME).when(clock).now();
          recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
        }

        @DisplayName("Then - 정상적으로 저장된다")
        @Test
        void ThenSaveSuccessfully() {

          recipeYoutubeMeta = repository.save(recipeYoutubeMeta);

          assertThat(recipeYoutubeMeta.getId()).isNotNull();
          assertThat(recipeYoutubeMeta.getVideoUri()).isEqualTo(youtubeUri.toUri());
          assertThat(recipeYoutubeMeta.getTitle()).isEqualTo(title);
          assertThat(recipeYoutubeMeta.getVideoSeconds()).isEqualTo(videoSeconds);
          assertThat(recipeYoutubeMeta.getThumbnailUrl()).isEqualTo(youtubeThumbnailUrl);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 유튜브 메타데이터 조회")
  class GetRecipeYoutubeMeta {

    private UriComponents youtubeUri;
    private String title;
    private Integer videoSeconds;
    private URI youtubeThumbnailUrl;
    private YoutubeVideoInfo youtubeVideoInfo;
    private RecipeYoutubeMeta recipeYoutubeMeta;
    private UUID recipeId;
    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
      youtubeUri =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + UUID.randomUUID())
              .build();
      title = "백종원의 요리비책 - 김치볶음밥";
      videoSeconds = 300;
      youtubeThumbnailUrl = URI.create("https://img.youtube.com/vi/j7s9VRsrm9o/maxresdefault.jpg");
      recipeId = UUID.randomUUID();

      youtubeVideoInfo = mock(YoutubeVideoInfo.class);
      doReturn(youtubeThumbnailUrl).when(youtubeVideoInfo).getThumbnailUrl();
      doReturn(title).when(youtubeVideoInfo).getTitle();
      doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
      doReturn(youtubeUri.toUri()).when(youtubeVideoInfo).getVideoUri();
      doReturn(FIXED_TIME).when(clock).now();

      recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
      repository.save(recipeYoutubeMeta);
    }

    @DisplayName("When - 유효한 URL로 조회한다면")
    @Nested
    class WhenGettingByValidUrl {

      private List<RecipeYoutubeMeta> recipeYoutubeMetaList;

      @BeforeEach
      void beforeEach() {
        recipeYoutubeMetaList = repository.findAllByVideoUri(youtubeUri.toUri());
      }

      @DisplayName("Then - 정상적으로 조회된다")
      @Test
      void ThenGetSuccessfully() {
        assertThat(recipeYoutubeMetaList).isNotNull();
        assertThat(recipeYoutubeMetaList).hasSize(1);
        RecipeYoutubeMeta foundRecipeYoutubeMeta = recipeYoutubeMetaList.get(0);
        assertThat(foundRecipeYoutubeMeta.getId()).isEqualTo(recipeYoutubeMeta.getId());
        assertThat(foundRecipeYoutubeMeta.getVideoUri()).isEqualTo(youtubeUri.toUri());
        assertThat(foundRecipeYoutubeMeta.getTitle()).isEqualTo(title);
        assertThat(foundRecipeYoutubeMeta.getVideoSeconds()).isEqualTo(videoSeconds);
        assertThat(foundRecipeYoutubeMeta.getThumbnailUrl()).isEqualTo(youtubeThumbnailUrl);
      }
    }

    @DisplayName("When - 유효한 레시피 메타 ID로 조회한다면")
    @Nested
    class WhenGettingByValidId {
      private RecipeYoutubeMeta foundRecipeYoutubeMeta;

      @BeforeEach
      void beforeEach() {
        foundRecipeYoutubeMeta = repository.findById(recipeYoutubeMeta.getId()).orElse(null);
      }

      @DisplayName("Then - 정상적으로 조회된다")
      @Test
      void ThenGetSuccessfully() {
        assertThat(foundRecipeYoutubeMeta).isNotNull();
        assertThat(foundRecipeYoutubeMeta.getId()).isEqualTo(recipeYoutubeMeta.getId());
        assertThat(foundRecipeYoutubeMeta.getVideoUri()).isEqualTo(youtubeUri.toUri());
        assertThat(foundRecipeYoutubeMeta.getTitle()).isEqualTo(title);
        assertThat(foundRecipeYoutubeMeta.getVideoSeconds()).isEqualTo(videoSeconds);
        assertThat(foundRecipeYoutubeMeta.getThumbnailUrl()).isEqualTo(youtubeThumbnailUrl);
      }
    }
  }

  @Nested
  @DisplayName("레시피 유튜브 메타데이터 목록 조회")
  class GetRecipeYoutubeMetas {

    private UriComponents youtubeUri1;
    private String title1;
    private Integer videoSeconds1;
    private URI youtubeThumbnailUrl1;
    private UriComponents youtubeUri2;
    private String title2;
    private Integer videoSeconds2;
    private URI youtubeThumbnailUrl2;
    private YoutubeVideoInfo youtubeVideoInfo1;
    private YoutubeVideoInfo youtubeVideoInfo2;
    private UUID recipeId1;
    private UUID recipeId2;
    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
      recipeId1 = UUID.randomUUID();
      recipeId2 = UUID.randomUUID();
      youtubeUri1 =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + UUID.randomUUID())
              .build();
      title1 = "백종원의 요리비책 - 김치볶음밥";
      videoSeconds1 = 300;
      youtubeThumbnailUrl1 = URI.create("https://img.youtube.com/vi/j7s9VRsrm9o/maxresdefault.jpg");

      youtubeUri2 =
          UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + UUID.randomUUID())
              .build();
      title2 = "백종원의 요리비책 - 된장찌개";
      videoSeconds2 = 600;
      youtubeThumbnailUrl2 = URI.create("https://img.youtube.com/vi/abcdEFGHIjk/maxresdefault.jpg");

      youtubeVideoInfo1 = mock(YoutubeVideoInfo.class);
      doReturn(youtubeThumbnailUrl1).when(youtubeVideoInfo1).getThumbnailUrl();
      doReturn(title1).when(youtubeVideoInfo1).getTitle();
      doReturn(videoSeconds1).when(youtubeVideoInfo1).getVideoSeconds();
      doReturn(youtubeUri1.toUri()).when(youtubeVideoInfo1).getVideoUri();

      youtubeVideoInfo2 = mock(YoutubeVideoInfo.class);
      doReturn(youtubeThumbnailUrl2).when(youtubeVideoInfo2).getThumbnailUrl();
      doReturn(title2).when(youtubeVideoInfo2).getTitle();
      doReturn(videoSeconds2).when(youtubeVideoInfo2).getVideoSeconds();
      doReturn(youtubeUri2.toUri()).when(youtubeVideoInfo2).getVideoUri();
      doReturn(FIXED_TIME).when(clock).now();
    }

    @DisplayName("When - 목록 조회한다면")
    @Nested
    class WhenGettingList {
      private Iterable<RecipeYoutubeMeta> recipeYoutubeMetas;

      RecipeYoutubeMeta recipeYoutubeMeta1;
      RecipeYoutubeMeta recipeYoutubeMeta2;
      List<UUID> youtubeMetaIds;
      List<UUID> recipeIds;

      @BeforeEach
      void beforeEach() {
        recipeYoutubeMeta1 = RecipeYoutubeMeta.create(youtubeVideoInfo1, recipeId1, clock);
        recipeYoutubeMeta2 = RecipeYoutubeMeta.create(youtubeVideoInfo2, recipeId2, clock);
        youtubeMetaIds =
            List.of(
                repository.save(recipeYoutubeMeta1).getId(),
                repository.save(recipeYoutubeMeta2).getId());
        recipeIds = List.of(recipeId1, recipeId2);
      }

      @DisplayName("Then - 정상적으로 조회된다")
      @Test
      void ThenGetListSuccessfully() {
        recipeYoutubeMetas = repository.findAllByRecipeIdIn(recipeIds);
        assertThat(recipeYoutubeMetas).isNotNull();
        assertThat(recipeYoutubeMetas).hasSize(2);
      }
    }
  }
}
