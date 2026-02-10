package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaClient;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaExternalClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.content.youtubemeta.repository.RecipeYoutubeMetaRepository;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeYoutubeMetaService 테스트")
public class RecipeYoutubeMetaServiceTest {

    private RecipeYoutubeMetaService service;
    private RecipeYoutubeMetaRepository repository;
    private YoutubeMetaClient client;
    private Clock clock;

    @BeforeEach
    public void setUp() {
        repository = mock(RecipeYoutubeMetaRepository.class);
        client = mock(YoutubeMetaExternalClient.class);
        clock = mock(Clock.class);
        service = new RecipeYoutubeMetaService(repository, client, clock);
    }

    @Nested
    @DisplayName("유튜브 메타 차단 (block)")
    class Block {

        @Nested
        @DisplayName("Given - 메타가 존재하고 영상이 차단된 경우")
        class GivenBlockedVideo {
            UUID recipeId;
            RecipeYoutubeMeta meta;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                recipeId = UUID.randomUUID();
                meta = mock(RecipeYoutubeMeta.class);
                doReturn(meta).when(repository).find(recipeId);
                doReturn(URI.create("https://www.youtube.com/watch?v=blocked"))
                        .when(meta)
                        .getVideoUri();
                doReturn(true).when(client).isBlocked(any(YoutubeUri.class));
            }

            @Nested
            @DisplayName("When - 차단을 요청하면")
            class WhenBlocking {

                @BeforeEach
                void setUp() throws YoutubeMetaException {
                    service.block(recipeId);
                }

                @Test
                @DisplayName("Then - 메타를 차단 처리한다")
                void thenBlocksMeta() throws YoutubeMetaException {
                    verify(client).isBlocked(any(YoutubeUri.class));
                    verify(repository).block(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 메타가 존재하지만 영상이 차단되지 않은 경우")
        class GivenNotBlockedVideo {
            UUID recipeId;
            RecipeYoutubeMeta meta;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                recipeId = UUID.randomUUID();
                meta = mock(RecipeYoutubeMeta.class);
                doReturn(meta).when(repository).find(recipeId);
                doReturn(URI.create("https://www.youtube.com/watch?v=notblocked"))
                        .when(meta)
                        .getVideoUri();
                doReturn(false).when(client).isBlocked(any(YoutubeUri.class));
            }

            @Nested
            @DisplayName("When - 차단을 요청하면")
            class WhenBlocking {

                @Test
                @DisplayName("Then - NOT_BLOCKED_VIDEO 예외를 던진다")
                void thenThrowsException() throws YoutubeMetaException {
                    assertThatThrownBy(() -> service.block(recipeId))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO);

                    verify(repository, never()).block(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 메타가 존재하지 않는 경우")
        class GivenMetaNotFound {
            UUID recipeId;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                recipeId = UUID.randomUUID();
                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
                        .when(repository)
                        .find(recipeId);
            }

            @Nested
            @DisplayName("When - 차단을 요청하면")
            class WhenBlocking {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.block(recipeId))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("유튜브 메타 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 비디오 정보가 주어졌을 때")
        class GivenValidInfo {
            YoutubeVideoInfo youtubeVideoInfo;
            UUID recipeId;
            RecipeYoutubeMeta recipeYoutubeMeta;

            @BeforeEach
            void setUp() {
                URI youtubeThumbnailUrl = UriComponentsBuilder.fromUriString(
                                "https://img.youtube.com/vi/test/default.jpg")
                        .build()
                        .toUri();
                YoutubeUri youtubeUri = mock(YoutubeUri.class);
                youtubeVideoInfo = YoutubeVideoInfo.from(
                        youtubeUri, "Sample Video", "Sample Channel", youtubeThumbnailUrl, 213, YoutubeMetaType.NORMAL);
                recipeId = UUID.randomUUID();
                recipeYoutubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
                doReturn(recipeYoutubeMeta).when(repository).create(any(RecipeYoutubeMeta.class));
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    service.create(youtubeVideoInfo, recipeId);
                }

                @Test
                @DisplayName("Then - 메타데이터를 저장한다")
                void thenSavesMeta() {
                    verify(repository).create(any(RecipeYoutubeMeta.class));
                }
            }
        }
    }

    @Nested
    @DisplayName("URL로 메타 조회 (getByUrl)")
    class GetByUrl {

        @Nested
        @DisplayName("Given - URL에 해당하는 메타데이터가 있을 때")
        class GivenMetaExists {
            URI originalUri;
            RecipeYoutubeMeta meta1;
            RecipeYoutubeMeta meta2;

            @BeforeEach
            void setUp() {
                originalUri = URI.create("https://www.youtube.com/watch?v=test");
                meta1 = mock(RecipeYoutubeMeta.class);
                meta2 = mock(RecipeYoutubeMeta.class);
            }

            @Nested
            @DisplayName("When - 모든 메타가 정상 상태일 때")
            class WhenAllNormal {

                @BeforeEach
                void setUp() {
                    doReturn(false).when(meta1).isBanned();
                    doReturn(false).when(meta1).isBlocked();
                    doReturn(YoutubeMetaStatus.ACTIVE).when(meta1).getStatus();

                    doReturn(false).when(meta2).isBanned();
                    doReturn(false).when(meta2).isBlocked();
                    doReturn(YoutubeMetaStatus.ACTIVE).when(meta2).getStatus();

                    doReturn(java.util.List.of(meta1, meta2)).when(repository).find(any(URI.class));
                }

                @Test
                @DisplayName("Then - 첫 번째 메타를 반환한다")
                void thenReturnsFirst() throws YoutubeMetaException {
                    RecipeYoutubeMeta result = service.getByUrl(originalUri);
                    assertThat(result).isEqualTo(meta1);
                }
            }

            @Nested
            @DisplayName("When - 차단된(banned) 메타가 있을 때")
            class WhenBanned {

                @BeforeEach
                void setUp() {
                    doReturn(java.util.List.of(meta1, meta2)).when(repository).find(any(URI.class));
                    doReturn(false).when(meta1).isBanned();
                    doReturn(true).when(meta2).isBanned();
                }

                @Test
                @DisplayName("Then - BANNED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.getByUrl(originalUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_BANNED);
                }
            }

            @Nested
            @DisplayName("When - 블락된(blocked) 메타가 있을 때")
            class WhenBlocked {

                @BeforeEach
                void setUp() {
                    doReturn(java.util.List.of(meta1, meta2)).when(repository).find(any(URI.class));
                    doReturn(false).when(meta1).isBanned();
                    doReturn(false).when(meta2).isBanned();
                    doReturn(false).when(meta1).isBlocked();
                    doReturn(true).when(meta2).isBlocked();
                }

                @Test
                @DisplayName("Then - BLOCKED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.getByUrl(originalUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED);
                }
            }
        }

        @Nested
        @DisplayName("Given - 메타데이터가 없을 때")
        class GivenNoMeta {
            URI originalUri;

            @BeforeEach
            void setUp() {
                originalUri = URI.create("https://www.youtube.com/watch?v=test");
                doReturn(java.util.List.of()).when(repository).find(any(URI.class));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.getByUrl(originalUri))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("비디오 정보 조회 (getVideoInfo)")
    class GetVideoInfo {

        @Nested
        @DisplayName("Given - 유효한 URL이 주어졌을 때")
        class GivenValidUrl {
            URI originalUri;
            YoutubeVideoInfo youtubeVideoInfo;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                originalUri = URI.create("https://www.youtube.com/watch?v=test");
                youtubeVideoInfo = mock(YoutubeVideoInfo.class);
                doReturn(youtubeVideoInfo).when(client).fetch(any(YoutubeUri.class));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 비디오 정보를 반환한다")
                void thenReturnsInfo() throws YoutubeMetaException {
                    YoutubeVideoInfo result = service.getVideoInfo(originalUri);
                    assertThat(result).isEqualTo(youtubeVideoInfo);
                }
            }
        }

        @Nested
        @DisplayName("Given - 클라이언트 오류 발생 시")
        class GivenClientError {
            URI originalUri;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                originalUri = URI.create("https://www.youtube.com/watch?v=test");
                doReturn(null).when(client).fetch(any(YoutubeUri.class));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - null을 반환한다")
                void thenReturnsNull() throws YoutubeMetaException {
                    YoutubeVideoInfo result = service.getVideoInfo(originalUri);
                    assertThat(result).isNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 ID로 메타 조회 (get)")
    class Get {

        @Nested
        @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
        class GivenExistingId {
            UUID recipeId;
            RecipeYoutubeMeta recipeYoutubeMeta;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                recipeId = UUID.randomUUID();
                recipeYoutubeMeta = mock(RecipeYoutubeMeta.class);
                doReturn(recipeYoutubeMeta).when(repository).find(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 메타데이터를 반환한다")
                void thenReturnsMeta() throws YoutubeMetaException {
                    RecipeYoutubeMeta result = service.get(recipeId);
                    assertThat(result).isEqualTo(recipeYoutubeMeta);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentId {
            UUID recipeId;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                recipeId = UUID.randomUUID();
                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
                        .when(repository)
                        .find(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.get(recipeId))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("다중 메타 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 레시피 ID 목록이 주어졌을 때")
        class GivenRecipeIds {
            UUID recipeId;
            RecipeYoutubeMeta recipeYoutubeMeta;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                recipeYoutubeMeta = mock(RecipeYoutubeMeta.class);
                doReturn(java.util.List.of(recipeYoutubeMeta)).when(repository).finds(any());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 메타데이터 목록을 반환한다")
                void thenReturnsList() {
                    var result = service.gets(java.util.List.of(recipeId));
                    assertThat(result).contains(recipeYoutubeMeta);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 목록이 주어졌을 때")
        class GivenEmptyList {

            @BeforeEach
            void setUp() {
                doReturn(java.util.List.of()).when(repository).finds(any());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() {
                    var result = service.gets(java.util.List.of());
                    assertThat(result).isEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("유튜브 메타 밴 (ban)")
    class Ban {

        @Nested
        @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
        class GivenExistingId {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 밴을 요청하면")
            class WhenBanning {

                @BeforeEach
                void setUp() throws YoutubeMetaException {
                    service.ban(recipeId);
                }

                @Test
                @DisplayName("Then - 메타데이터를 밴 처리한다")
                void thenBansMeta() throws YoutubeMetaException {
                    verify(repository).ban(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentId {
            UUID recipeId;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                recipeId = UUID.randomUUID();
                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
                        .when(repository)
                        .ban(recipeId);
            }

            @Nested
            @DisplayName("When - 밴을 요청하면")
            class WhenBanning {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.ban(recipeId))
                            .isInstanceOf(YoutubeMetaException.class)
                            .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
                }
            }
        }
    }
}
