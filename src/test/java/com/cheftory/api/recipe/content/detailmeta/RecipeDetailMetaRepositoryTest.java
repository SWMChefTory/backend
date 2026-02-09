package com.cheftory.api.recipe.content.detailmeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.content.detailMeta.repository.RecipeDetailMetaRepository;
import com.cheftory.api.recipe.content.detailMeta.repository.RecipeDetailMetaRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({RecipeDetailMetaRepositoryImpl.class})
@DisplayName("RecipeDetailMetaRepository 테스트")
class RecipeDetailMetaRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeDetailMetaRepository recipeDetailMetaRepository;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("get 메서드는")
    class Describe_get {

        @Test
        @DisplayName("데이터가 존재하면 정상적으로 조회한다")
        void it_returns_data_when_exists() throws RecipeDetailMetaException {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeDetailMeta created = RecipeDetailMeta.create(30, 4, "Test description", clock, recipeId);
            recipeDetailMetaRepository.create(created);

            // When
            RecipeDetailMeta result = recipeDetailMetaRepository.get(recipeId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecipeId()).isEqualTo(recipeId);
            assertThat(result.getCookTime()).isEqualTo(30);
            assertThat(result.getServings()).isEqualTo(4);
            assertThat(result.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("데이터가 존재하지 않으면 예외를 던진다")
        void it_throws_exception_when_not_exists() {
            // Given
            UUID recipeId = UUID.randomUUID();

            // When & Then
            assertThatThrownBy(() -> recipeDetailMetaRepository.get(recipeId))
                    .isInstanceOf(RecipeDetailMetaException.class)
                    .extracting("error")
                    .isEqualTo(RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("gets 메서드는")
    class Describe_gets {

        @Test
        @DisplayName("여러 레시피 ID에 대한 메타를 정상적으로 조회한다")
        void it_returns_multiple_recipe_detail_metas() {
            // Given
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();
            UUID recipeId3 = UUID.randomUUID();

            RecipeDetailMeta meta1 = RecipeDetailMeta.create(30, 4, "Description 1", clock, recipeId1);
            RecipeDetailMeta meta2 = RecipeDetailMeta.create(45, 2, "Description 2", clock, recipeId2);
            RecipeDetailMeta meta3 = RecipeDetailMeta.create(60, 6, "Description 3", clock, recipeId3);

            recipeDetailMetaRepository.create(meta1);
            recipeDetailMetaRepository.create(meta2);
            recipeDetailMetaRepository.create(meta3);

            List<UUID> recipeIds = List.of(recipeId1, recipeId2, recipeId3);

            // When
            List<RecipeDetailMeta> result = recipeDetailMetaRepository.gets(recipeIds);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.stream().map(RecipeDetailMeta::getRecipeId))
                    .containsExactlyInAnyOrder(recipeId1, recipeId2, recipeId3);
        }

        @Test
        @DisplayName("빈 리스트를 전달하면 빈 리스트를 반환한다")
        void it_returns_empty_list_when_input_is_empty() {
            // Given
            List<UUID> recipeIds = List.of();

            // When
            List<RecipeDetailMeta> result = recipeDetailMetaRepository.gets(recipeIds);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("일부 데이터만 존재하면 존재하는 데이터만 반환한다")
        void it_returns_only_existing_data() {
            // Given
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();
            UUID recipeId3 = UUID.randomUUID();

            RecipeDetailMeta meta1 = RecipeDetailMeta.create(30, 4, "Description 1", clock, recipeId1);
            RecipeDetailMeta meta3 = RecipeDetailMeta.create(60, 6, "Description 3", clock, recipeId3);

            recipeDetailMetaRepository.create(meta1);
            recipeDetailMetaRepository.create(meta3);

            List<UUID> recipeIds = List.of(recipeId1, recipeId2, recipeId3);

            // When
            List<RecipeDetailMeta> result = recipeDetailMetaRepository.gets(recipeIds);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.stream().map(RecipeDetailMeta::getRecipeId))
                    .containsExactlyInAnyOrder(recipeId1, recipeId3);
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("새로운 RecipeDetailMeta를 생성한다")
        void it_creates_new_recipe_detail_meta() throws RecipeDetailMetaException {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeDetailMeta recipeDetailMeta = RecipeDetailMeta.create(45, 2, "Test description", clock, recipeId);

            // When
            recipeDetailMetaRepository.create(recipeDetailMeta);

            // Then
            RecipeDetailMeta result = recipeDetailMetaRepository.get(recipeId);
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getRecipeId()).isEqualTo(recipeId);
            assertThat(result.getCookTime()).isEqualTo(45);
            assertThat(result.getServings()).isEqualTo(2);
            assertThat(result.getDescription()).isEqualTo("Test description");
        }
    }
}
