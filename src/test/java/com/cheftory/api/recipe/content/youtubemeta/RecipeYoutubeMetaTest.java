package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeYoutubeMeta 엔티티")
public class RecipeYoutubeMetaTest {

    @Nested
    @DisplayName("레시피 유튜브 메타데이터 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            URI videoUri;
            String title;
            String thumbnailUrl;
            Integer videoSeconds;
            YoutubeVideoInfo youtubeVideoInfo;
            Clock clock;
            LocalDateTime now;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                videoUri = URI.create("https://www.youtube.com/watch?v=testvideoid");
                title = "Sample Video";
                thumbnailUrl = "https://img.youtube.com/vi/testvideoid/test.jpg";
                videoSeconds = 213;
                clock = mock(Clock.class);
                youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                now = LocalDateTime.now();
                recipeId = UUID.randomUUID();

                doReturn(videoUri).when(youtubeVideoInfo).getVideoUri();
                doReturn(title).when(youtubeVideoInfo).getTitle();
                doReturn("Sample Channel").when(youtubeVideoInfo).getChannelTitle();
                doReturn(URI.create(thumbnailUrl)).when(youtubeVideoInfo).getThumbnailUrl();
                doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
                doReturn(now).when(clock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeYoutubeMeta recipeYoutubeMeta;

                @BeforeEach
                void setUp() {
                    recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
                }

                @Test
                @DisplayName("Then - 메타데이터가 올바르게 생성된다")
                void thenCreatedCorrectly() {
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
        }
    }

    @Nested
    @DisplayName("상태 변경 (ban, block)")
    class StatusChange {

        @Nested
        @DisplayName("Given - 생성된 메타데이터가 있을 때")
        class GivenCreatedMeta {
            RecipeYoutubeMeta recipeYoutubeMeta;
            YoutubeVideoInfo youtubeVideoInfo;
            Clock clock;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                clock = mock(Clock.class);
                youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                recipeId = UUID.randomUUID();
                doReturn(URI.create("https://youtube.com/watch?v=1"))
                        .when(youtubeVideoInfo)
                        .getVideoUri();
                doReturn(LocalDateTime.now()).when(clock).now();
                recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 밴을 요청하면")
            class WhenBanning {

                @BeforeEach
                void setUp() {
                    recipeYoutubeMeta.ban();
                }

                @Test
                @DisplayName("Then - BANNED 상태가 된다")
                void thenBanned() {
                    assertTrue(recipeYoutubeMeta.isBanned());
                    assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.BANNED);
                }
            }

            @Nested
            @DisplayName("When - 블락을 요청하면")
            class WhenBlocking {

                @BeforeEach
                void setUp() {
                    recipeYoutubeMeta.block();
                }

                @Test
                @DisplayName("Then - BLOCKED 상태가 된다")
                void thenBlocked() {
                    assertTrue(recipeYoutubeMeta.isBlocked());
                    assertFalse(recipeYoutubeMeta.isBanned());
                    assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.BLOCKED);
                }
            }
        }
    }

    @Nested
    @DisplayName("비디오 타입 (type)")
    class VideoType {

        @Nested
        @DisplayName("Given - NORMAL 타입 정보가 주어졌을 때")
        class GivenNormalType {
            YoutubeVideoInfo videoInfo;
            UUID recipeId;
            Clock clock;

            @BeforeEach
            void setUp() {
                videoInfo = mock(YoutubeVideoInfo.class);
                recipeId = UUID.randomUUID();
                clock = mock(Clock.class);
                doReturn(URI.create("https://youtube.com/watch?v=1"))
                        .when(videoInfo)
                        .getVideoUri();
                doReturn(LocalDateTime.now()).when(clock).now();
                doReturn(YoutubeMetaType.NORMAL).when(videoInfo).getVideoType();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeYoutubeMeta meta;

                @BeforeEach
                void setUp() {
                    meta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
                }

                @Test
                @DisplayName("Then - NORMAL 타입으로 생성된다")
                void thenNormalType() {
                    assertThat(meta.getType()).isEqualTo(YoutubeMetaType.NORMAL);
                }
            }
        }

        @Nested
        @DisplayName("Given - SHORTS 타입 정보가 주어졌을 때")
        class GivenShortsType {
            YoutubeVideoInfo videoInfo;
            UUID recipeId;
            Clock clock;

            @BeforeEach
            void setUp() {
                videoInfo = mock(YoutubeVideoInfo.class);
                recipeId = UUID.randomUUID();
                clock = mock(Clock.class);
                doReturn(URI.create("https://youtube.com/watch?v=1"))
                        .when(videoInfo)
                        .getVideoUri();
                doReturn(LocalDateTime.now()).when(clock).now();
                doReturn(YoutubeMetaType.SHORTS).when(videoInfo).getVideoType();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeYoutubeMeta meta;

                @BeforeEach
                void setUp() {
                    meta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
                }

                @Test
                @DisplayName("Then - SHORTS 타입으로 생성된다")
                void thenShortsType() {
                    assertThat(meta.getType()).isEqualTo(YoutubeMetaType.SHORTS);
                }
            }
        }
    }
}
