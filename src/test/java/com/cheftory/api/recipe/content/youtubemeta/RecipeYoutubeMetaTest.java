package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
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
class RecipeYoutubeMetaTest {

    @Nested
    @DisplayName("생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 비디오 정보가 주어졌을 때")
        class GivenValidVideoInfo {
            Clock clock;
            YoutubeVideoInfo youtubeVideoInfo;
            LocalDateTime now;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                clock = mock(Clock.class);
                youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                now = LocalDateTime.of(2026, 1, 1, 0, 0);
                recipeId = UUID.randomUUID();

                doReturn(now).when(clock).now();
                doReturn("testvideoid").when(youtubeVideoInfo).getVideoId();
                doReturn("Sample Video").when(youtubeVideoInfo).getTitle();
                doReturn("Sample Channel").when(youtubeVideoInfo).getChannelTitle();
                doReturn(URI.create("https://img.youtube.com/vi/testvideoid/default.jpg"))
                        .when(youtubeVideoInfo)
                        .getThumbnailUrl();
                doReturn(213).when(youtubeVideoInfo).getVideoSeconds();
                doReturn(YoutubeMetaType.NORMAL).when(youtubeVideoInfo).getVideoType();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeYoutubeMeta result;

                @BeforeEach
                void setUp() {
                    result = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
                }

                @Test
                @DisplayName("Then - videoId 기반 메타가 생성된다")
                void thenCreatesVideoIdBasedMeta() {
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getRecipeId()).isEqualTo(recipeId);
                    assertThat(result.getVideoId()).isEqualTo("testvideoid");
                    assertThat(result.getTitle()).isEqualTo("Sample Video");
                    assertThat(result.getChannelTitle()).isEqualTo("Sample Channel");
                    assertThat(result.getCreatedAt()).isEqualTo(now);
                    assertThat(result.getType()).isEqualTo(YoutubeMetaType.NORMAL);
                    assertThat(result.getVideoUri())
                            .isEqualTo(URI.create("https://www.youtube.com/watch?v=testvideoid"));
                }
            }
        }

        @Nested
        @DisplayName("Given - SHORTS 타입 비디오 정보가 주어졌을 때")
        class GivenShortsVideoInfo {
            Clock clock;
            YoutubeVideoInfo youtubeVideoInfo;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                clock = mock(Clock.class);
                youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                recipeId = UUID.randomUUID();

                doReturn(LocalDateTime.of(2026, 1, 1, 1, 0)).when(clock).now();
                doReturn("shorts-id").when(youtubeVideoInfo).getVideoId();
                doReturn("Shorts").when(youtubeVideoInfo).getTitle();
                doReturn("Shorts Channel").when(youtubeVideoInfo).getChannelTitle();
                doReturn(URI.create("https://img.youtube.com/vi/shorts-id/default.jpg"))
                        .when(youtubeVideoInfo)
                        .getThumbnailUrl();
                doReturn(59).when(youtubeVideoInfo).getVideoSeconds();
                doReturn(YoutubeMetaType.SHORTS).when(youtubeVideoInfo).getVideoType();
            }

            @Test
            @DisplayName("When/Then - 타입과 videoUri 계산값이 유지된다")
            void preservesTypeAndComputedUri() {
                RecipeYoutubeMeta result = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);

                assertThat(result.getType()).isEqualTo(YoutubeMetaType.SHORTS);
                assertThat(result.getVideoUri()).isEqualTo(URI.create("https://www.youtube.com/watch?v=shorts-id"));
            }
        }
    }
}
