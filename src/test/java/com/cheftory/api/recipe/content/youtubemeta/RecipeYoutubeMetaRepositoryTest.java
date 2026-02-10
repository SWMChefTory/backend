package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.content.youtubemeta.repository.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipe.content.youtubemeta.repository.RecipeYoutubeMetaRepositoryImpl;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeYoutubeMetaRepository 테스트")
@Import(RecipeYoutubeMetaRepositoryImpl.class)
public class RecipeYoutubeMetaRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeYoutubeMetaRepository repository;

    @MockitoBean
    private Clock clock;

    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        doReturn(FIXED_TIME).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 유튜브 메타데이터 저장 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 메타데이터가 주어졌을 때")
        class GivenValidMeta {
            UriComponents youtubeUri;
            String title;
            Integer videoSeconds;
            URI youtubeThumbnailUrl;
            YoutubeVideoInfo youtubeVideoInfo;
            RecipeYoutubeMeta recipeYoutubeMeta;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                youtubeUri = UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=j7s9VRsrm9o")
                        .build();
                title = "백종원의 요리비책 - 김치볶음밥";
                videoSeconds = 300;
                youtubeThumbnailUrl = URI.create("https://img.youtube.com/vi/j7s9VRsrm9o/maxresdefault.jpg");
                recipeId = UUID.randomUUID();

                youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                doReturn(youtubeThumbnailUrl).when(youtubeVideoInfo).getThumbnailUrl();
                doReturn(title).when(youtubeVideoInfo).getTitle();
                doReturn("백종원 채널").when(youtubeVideoInfo).getChannelTitle();
                doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
                doReturn(youtubeUri.toUri()).when(youtubeVideoInfo).getVideoUri();
                doReturn(YoutubeMetaType.NORMAL).when(youtubeVideoInfo).getVideoType();

                recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {
                RecipeYoutubeMeta result;

                @BeforeEach
                void setUp() {
                    result = repository.create(recipeYoutubeMeta);
                }

                @Test
                @DisplayName("Then - 정상적으로 저장된다")
                void thenSavesSuccessfully() {
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getVideoUri()).isEqualTo(youtubeUri.toUri());
                    assertThat(result.getTitle()).isEqualTo(title);
                    assertThat(result.getVideoSeconds()).isEqualTo(videoSeconds);
                    assertThat(result.getThumbnailUrl()).isEqualTo(youtubeThumbnailUrl);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 유튜브 메타데이터 조회 (find)")
    class Find {

        @Nested
        @DisplayName("Given - 메타데이터가 저장되어 있을 때")
        class GivenSavedMeta {
            UriComponents youtubeUri;
            String title;
            Integer videoSeconds;
            URI youtubeThumbnailUrl;
            RecipeYoutubeMeta savedMeta;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                youtubeUri = UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + UUID.randomUUID())
                        .build();
                title = "백종원의 요리비책 - 김치볶음밥";
                videoSeconds = 300;
                youtubeThumbnailUrl = URI.create("https://img.youtube.com/vi/j7s9VRsrm9o/maxresdefault.jpg");
                recipeId = UUID.randomUUID();

                YoutubeVideoInfo youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                doReturn(youtubeThumbnailUrl).when(youtubeVideoInfo).getThumbnailUrl();
                doReturn(title).when(youtubeVideoInfo).getTitle();
                doReturn("백종원 채널").when(youtubeVideoInfo).getChannelTitle();
                doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
                doReturn(youtubeUri.toUri()).when(youtubeVideoInfo).getVideoUri();
                doReturn(YoutubeMetaType.NORMAL).when(youtubeVideoInfo).getVideoType();

                savedMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
                repository.create(savedMeta);
            }

            @Nested
            @DisplayName("When - URL로 조회하면")
            class WhenFindingByUrl {
                List<RecipeYoutubeMeta> result;

                @BeforeEach
                void setUp() {
                    result = repository.find(youtubeUri.toUri());
                }

                @Test
                @DisplayName("Then - 해당 메타데이터를 반환한다")
                void thenReturnsMeta() {
                    assertThat(result).hasSize(1);
                    RecipeYoutubeMeta found = result.getFirst();
                    assertThat(found.getId()).isEqualTo(savedMeta.getId());
                    assertThat(found.getVideoUri()).isEqualTo(youtubeUri.toUri());
                    assertThat(found.getTitle()).isEqualTo(title);
                }
            }

            @Nested
            @DisplayName("When - 레시피 ID로 조회하면")
            class WhenFindingById {
                RecipeYoutubeMeta result;

                @BeforeEach
                void setUp() throws YoutubeMetaException {
                    result = repository.find(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 메타데이터를 반환한다")
                void thenReturnsMeta() {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isEqualTo(savedMeta.getId());
                    assertThat(result.getVideoUri()).isEqualTo(youtubeUri.toUri());
                    assertThat(result.getTitle()).isEqualTo(title);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 유튜브 메타데이터 목록 조회 (finds)")
    class Finds {

        @Nested
        @DisplayName("Given - 여러 메타데이터가 저장되어 있을 때")
        class GivenMultipleMetas {
            List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                UUID recipeId1 = UUID.randomUUID();
                UUID recipeId2 = UUID.randomUUID();

                YoutubeVideoInfo info1 = mock(YoutubeVideoInfo.class);
                doReturn(URI.create("https://img.youtube.com/vi/1/default.jpg"))
                        .when(info1)
                        .getThumbnailUrl();
                doReturn("Title 1").when(info1).getTitle();
                doReturn("Channel 1").when(info1).getChannelTitle();
                doReturn(300).when(info1).getVideoSeconds();
                doReturn(URI.create("https://youtube.com/watch?v=1"))
                        .when(info1)
                        .getVideoUri();
                doReturn(YoutubeMetaType.NORMAL).when(info1).getVideoType();

                YoutubeVideoInfo info2 = mock(YoutubeVideoInfo.class);
                doReturn(URI.create("https://img.youtube.com/vi/2/default.jpg"))
                        .when(info2)
                        .getThumbnailUrl();
                doReturn("Title 2").when(info2).getTitle();
                doReturn("Channel 2").when(info2).getChannelTitle();
                doReturn(600).when(info2).getVideoSeconds();
                doReturn(URI.create("https://youtube.com/watch?v=2"))
                        .when(info2)
                        .getVideoUri();
                doReturn(YoutubeMetaType.NORMAL).when(info2).getVideoType();

                RecipeYoutubeMeta meta1 = RecipeYoutubeMeta.create(info1, recipeId1, clock);
                RecipeYoutubeMeta meta2 = RecipeYoutubeMeta.create(info2, recipeId2, clock);

                repository.create(meta1);
                repository.create(meta2);

                recipeIds = List.of(recipeId1, recipeId2);
            }

            @Nested
            @DisplayName("When - 목록 조회를 요청하면")
            class WhenFindingList {
                Iterable<RecipeYoutubeMeta> result;

                @BeforeEach
                void setUp() {
                    result = repository.finds(recipeIds);
                }

                @Test
                @DisplayName("Then - 모든 메타데이터를 반환한다")
                void thenReturnsAll() {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(2);
                }
            }
        }
    }
}
