package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.content.youtubemeta.repository.RecipeYoutubeMetaRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeYoutubeMetaService 테스트")
class RecipeYoutubeMetaServiceTest {

    private RecipeYoutubeMetaRepository repository;
    private YoutubeMetaClient client;
    private Clock clock;
    private RecipeYoutubeMetaService service;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeYoutubeMetaRepository.class);
        client = mock(YoutubeMetaClient.class);
        clock = mock(Clock.class);
        service = new RecipeYoutubeMetaService(repository, client, clock);
    }

    @Nested
    @DisplayName("생성 (create)")
    class Create {
        @Test
        @DisplayName("repository.create 로 위임한다")
        void delegatesToRepository() throws YoutubeMetaException {
            YoutubeVideoInfo info = mock(YoutubeVideoInfo.class);
            UUID recipeId = UUID.randomUUID();

            service.create(info, recipeId);

            verify(repository).create(any(RecipeYoutubeMeta.class));
        }

        @Test
        @DisplayName("repository 런타임 예외를 그대로 전파한다")
        void propagatesRepositoryException() {
            YoutubeVideoInfo info = mock(YoutubeVideoInfo.class);
            UUID recipeId = UUID.randomUUID();
            RuntimeException exception = new RuntimeException("db-error");
            org.mockito.Mockito.doThrow(exception).when(repository).create(any(RecipeYoutubeMeta.class));

            assertThatThrownBy(() -> service.create(info, recipeId)).isSameAs(exception);
        }
    }

    @Nested
    @DisplayName("존재 여부 확인 (exists)")
    class Exists {
        @Test
        @DisplayName("repository.exists 로 위임한다")
        void delegates() {
            UUID recipeId = UUID.randomUUID();
            doReturn(true).when(repository).exists(recipeId);

            boolean result = service.exists(recipeId);

            assertThat(result).isTrue();
            verify(repository).exists(recipeId);
        }

        @Test
        @DisplayName("repository.exists 결과 false를 그대로 반환한다")
        void delegatesFalse() {
            UUID recipeId = UUID.randomUUID();
            doReturn(false).when(repository).exists(recipeId);

            boolean result = service.exists(recipeId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("비디오 정보 조회 (getVideoInfo)")
    class GetVideoInfo {
        @Test
        @DisplayName("videoId로 client.fetch 를 호출한다")
        void fetchesByVideoId() throws YoutubeMetaException {
            String videoId = "video-123";
            YoutubeVideoInfo expected = mock(YoutubeVideoInfo.class);
            doReturn(expected).when(client).fetch(videoId);

            YoutubeVideoInfo result = service.getVideoInfo(videoId);

            assertThat(result).isEqualTo(expected);
            verify(client).fetch(videoId);
        }

        @Test
        @DisplayName("client 예외를 그대로 전파한다")
        void propagatesClientException() throws YoutubeMetaException {
            String videoId = "video-123";
            YoutubeMetaException exception = new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_API_ERROR);
            org.mockito.Mockito.doThrow(exception).when(client).fetch(videoId);

            assertThatThrownBy(() -> service.getVideoInfo(videoId)).isSameAs(exception);
        }
    }

    @Nested
    @DisplayName("조회 (get, gets)")
    class Get {
        @Test
        @DisplayName("recipeId로 단건 메타를 조회한다")
        void getByRecipeId() throws YoutubeMetaException {
            UUID recipeId = UUID.randomUUID();
            RecipeYoutubeMeta meta = mock(RecipeYoutubeMeta.class);
            doReturn(meta).when(repository).find(recipeId);

            RecipeYoutubeMeta result = service.get(recipeId);

            assertThat(result).isEqualTo(meta);
            verify(repository).find(recipeId);
        }

        @Test
        @DisplayName("recipeId 목록으로 메타 목록을 조회한다")
        void getsByRecipeIds() {
            List<UUID> recipeIds = List.of(UUID.randomUUID());
            List<RecipeYoutubeMeta> expected = List.of(mock(RecipeYoutubeMeta.class));
            doReturn(expected).when(repository).finds(recipeIds);

            List<RecipeYoutubeMeta> result = service.gets(recipeIds);

            assertThat(result).isEqualTo(expected);
            verify(repository).finds(recipeIds);
        }
    }

    @Nested
    @DisplayName("videoId 추출 (getVideoId)")
    class GetVideoId {
        @Test
        @DisplayName("URL에서 videoId를 추출한다")
        void extractsFromUri() throws YoutubeMetaException {
            URI original = URI.create("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

            String result = service.getVideoId(original);

            assertThat(result).isEqualTo(YoutubeUri.from(original).getVideoId());
        }

        @Test
        @DisplayName("유효하지 않은 URL이면 예외를 전파한다")
        void throwsWhenInvalidUri() {
            URI invalid = URI.create("https://example.com/not-youtube");

            assertThatThrownBy(() -> service.getVideoId(invalid))
                    .isInstanceOf(YoutubeMetaException.class)
                    .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID);
        }
    }
}
