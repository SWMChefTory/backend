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
                doReturn("Sample Channel").when(youtubeVideoInfo).getChannelTitle();
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
                doReturn("Sample Channel").when(youtubeVideoInfo).getChannelTitle();
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

    @Nested
    @DisplayName("레시피 유튜브 메타데이터 블락")
    class BlockRecipeYoutubeMeta {

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
                doReturn("Sample Channel").when(youtubeVideoInfo).getChannelTitle();
                doReturn(URI.create(thumbnailUrl)).when(youtubeVideoInfo).getThumbnailUrl();
                doReturn(videoSeconds).when(youtubeVideoInfo).getVideoSeconds();
                doReturn(now).when(clock).now();

                recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 유튜브 메타데이터를 블락한다면")
            class WhenBlockingYoutubeMeta {

                @BeforeEach
                void beforeEach() {
                    recipeYoutubeMeta.block();
                }

                @Test
                @DisplayName("Then - 레시피 유튜브 메타데이터가 블락된다.")
                void itBlocksRecipeYoutubeMeta() {
                    assertThat(recipeYoutubeMeta).isNotNull();
                    assertTrue(recipeYoutubeMeta.isBlocked());
                    assertFalse(recipeYoutubeMeta.isBanned());
                    assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.BLOCKED);
                }
            }

            @Nested
            @DisplayName("When - 유튜브 메타데이터가 블락되었는지 확인한다면")
            class WhenCheckingIfBlocked {

                @Test
                @DisplayName("Then - 레시피 유튜브 메타데이터가 블락되지 않았다.")
                void itIsNotBlocked() {
                    assertThat(recipeYoutubeMeta).isNotNull();
                    assertFalse(recipeYoutubeMeta.isBlocked());
                    assertThat(recipeYoutubeMeta.getStatus()).isEqualTo(YoutubeMetaStatus.ACTIVE);
                }
            }
        }
    }

    @Nested
    @DisplayName("비디오 타입 설정")
    class VideoTypeField {

        private Clock clock;
        private UUID recipeId;
        private URI videoUri;
        private LocalDateTime now;

        @BeforeEach
        void setUp() {
            clock = mock(Clock.class);
            recipeId = UUID.randomUUID();
            videoUri = URI.create("https://www.youtube.com/watch?v=testid");
            now = LocalDateTime.now();
            doReturn(now).when(clock).now();
        }

        @Test
        @DisplayName("NORMAL 타입 비디오 정보로 생성하면 type이 NORMAL이다")
        void createsWithNormalType() {
            YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
            doReturn(videoUri).when(videoInfo).getVideoUri();
            doReturn("Normal Video").when(videoInfo).getTitle();
            doReturn("Sample Channel").when(videoInfo).getChannelTitle();
            doReturn(URI.create("https://img.youtube.com/vi/testid/default.jpg"))
                    .when(videoInfo)
                    .getThumbnailUrl();
            doReturn(300).when(videoInfo).getVideoSeconds();
            doReturn(YoutubeMetaType.NORMAL).when(videoInfo).getVideoType();

            RecipeYoutubeMeta meta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);

            assertThat(meta.getType()).isEqualTo(YoutubeMetaType.NORMAL);
        }

        @Test
        @DisplayName("SHORTS 타입 비디오 정보로 생성하면 type이 SHORTS이다")
        void createsWithShortsType() {
            YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
            doReturn(videoUri).when(videoInfo).getVideoUri();
            doReturn("Shorts Video").when(videoInfo).getTitle();
            doReturn("Sample Channel").when(videoInfo).getChannelTitle();
            doReturn(URI.create("https://img.youtube.com/vi/testid/default.jpg"))
                    .when(videoInfo)
                    .getThumbnailUrl();
            doReturn(30).when(videoInfo).getVideoSeconds();
            doReturn(YoutubeMetaType.SHORTS).when(videoInfo).getVideoType();

            RecipeYoutubeMeta meta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);

            assertThat(meta.getType()).isEqualTo(YoutubeMetaType.SHORTS);
        }
    }
}
