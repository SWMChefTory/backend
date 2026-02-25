package com.cheftory.api.recipe.content.youtubemeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
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

@DisplayName("RecipeYoutubeMetaRepository 테스트")
@Import(RecipeYoutubeMetaRepositoryImpl.class)
class RecipeYoutubeMetaRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeYoutubeMetaRepository repository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        doReturn(LocalDateTime.of(2026, 1, 1, 12, 0)).when(clock).now();
    }

    @Nested
    @DisplayName("저장/단건 조회")
    class CreateAndFindByRecipeId {
        UUID recipeId;
        RecipeYoutubeMeta meta;

        @BeforeEach
        void setUp() {
            recipeId = UUID.randomUUID();
            meta = RecipeYoutubeMeta.create(newVideoInfo("video-123"), recipeId, clock);
        }

        @Nested
        @DisplayName("When - 저장 후 recipeId로 조회하면")
        class WhenSavingAndFinding {
            RecipeYoutubeMeta found;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                repository.create(meta);
                found = repository.find(recipeId);
            }

            @Test
            @DisplayName("Then - 저장된 메타를 반환한다")
            void thenReturnsSavedMeta() {
                assertThat(found.getRecipeId()).isEqualTo(recipeId);
                assertThat(found.getVideoId()).isEqualTo("video-123");
                assertThat(repository.exists(recipeId)).isTrue();
            }
        }

        @Nested
        @DisplayName("When - 같은 recipeId로 중복 저장을 시도하면")
        class WhenDuplicateCreate {

            @BeforeEach
            void setUp() {
                repository.create(meta);
                repository.create(meta);
            }

            @Test
            @DisplayName("Then - 예외 없이 1건만 유지된다")
            void thenKeepsSingleRow() throws YoutubeMetaException {
                RecipeYoutubeMeta found = repository.find(recipeId);
                assertThat(found.getRecipeId()).isEqualTo(recipeId);
                assertThat(repository.finds(List.of(recipeId))).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("videoId 조회")
    class FindByVideoId {
        UUID recipeId1;
        UUID recipeId2;
        String videoId;

        @BeforeEach
        void setUp() {
            recipeId1 = UUID.randomUUID();
            recipeId2 = UUID.randomUUID();
            videoId = "v-" + UUID.randomUUID().toString().substring(0, 8);
            repository.create(RecipeYoutubeMeta.create(newVideoInfo(videoId), recipeId1, clock));
            repository.create(RecipeYoutubeMeta.create(newVideoInfo(videoId), recipeId2, clock));
        }

        @Test
        @DisplayName("같은 videoId 메타 목록을 반환한다")
        void returnsMetas() {
            List<RecipeYoutubeMeta> result = repository.find(videoId);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(RecipeYoutubeMeta::getRecipeId).contains(recipeId1, recipeId2);
        }

        @Test
        @DisplayName("존재하지 않는 videoId면 빈 목록을 반환한다")
        void returnsEmptyForUnknownVideoId() {
            assertThat(repository.find("unknown-video")).isEmpty();
        }
    }

    @Nested
    @DisplayName("레시피 ID 목록 조회 (finds)")
    class Finds {
        @Test
        @DisplayName("요청한 recipeId 목록에 해당하는 메타만 반환한다")
        void returnsRequestedRecipeMetas() {
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();
            UUID recipeId3 = UUID.randomUUID();
            repository.create(RecipeYoutubeMeta.create(newVideoInfo("v1"), recipeId1, clock));
            repository.create(RecipeYoutubeMeta.create(newVideoInfo("v2"), recipeId2, clock));
            repository.create(RecipeYoutubeMeta.create(newVideoInfo("v3"), recipeId3, clock));

            List<RecipeYoutubeMeta> result = repository.finds(List.of(recipeId1, recipeId3));

            assertThat(result)
                    .extracting(RecipeYoutubeMeta::getRecipeId)
                    .containsExactlyInAnyOrder(recipeId1, recipeId3);
        }
    }

    @Nested
    @DisplayName("단건 조회 실패 (find by recipeId)")
    class FindByRecipeId {
        @Test
        @DisplayName("존재하지 않는 recipeId면 NOT_FOUND 예외를 던진다")
        void throwsNotFound() {
            assertThatThrownBy(() -> repository.find(UUID.randomUUID()))
                    .isInstanceOf(YoutubeMetaException.class)
                    .hasFieldOrPropertyWithValue("error", YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
        }

        @Test
        @DisplayName("exists는 존재하지 않는 recipeId에 대해 false를 반환한다")
        void existsReturnsFalse() {
            assertThat(repository.exists(UUID.randomUUID())).isFalse();
        }
    }

    private YoutubeVideoInfo newVideoInfo(String videoId) {
        YoutubeVideoInfo info = mock(YoutubeVideoInfo.class);
        doReturn(videoId).when(info).getVideoId();
        doReturn("title-" + videoId).when(info).getTitle();
        doReturn("channel-" + videoId).when(info).getChannelTitle();
        doReturn(URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"))
                .when(info)
                .getThumbnailUrl();
        doReturn(120).when(info).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(info).getVideoType();
        return info;
    }
}
