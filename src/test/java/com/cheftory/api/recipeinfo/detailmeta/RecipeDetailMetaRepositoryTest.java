package com.cheftory.api.recipeinfo.detailmeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMetaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeDetailMetaRepositoryTest")
public class RecipeDetailMetaRepositoryTest extends DbContextTest {

  @Autowired private RecipeDetailMetaRepository recipeDetailMetaRepository;
  @MockitoBean private Clock clock;

  @Nested
  @DisplayName("레시피 상세 메타 저장")
  class SaveRecipeDetailMeta {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private UUID recipeId;
      private Integer cookTime;
      private Integer servings;
      private String description;
      private LocalDateTime createdAt;

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        recipeId = UUID.randomUUID();
        cookTime = 30;
        servings = 2;
        description = "맛있는 김치찌개 만들기";
        createdAt = LocalDateTime.now();
        doReturn(createdAt).when(clock).now();
      }

      @DisplayName("When - 레시피 상세 메타를 저장하면")
      @Nested
      class WhenSaveRecipeDetailMeta {

        private RecipeDetailMeta recipeDetailMeta;

        @BeforeEach
        void setUp() {
          recipeDetailMeta = RecipeDetailMeta.create(cookTime, servings, description, clock, recipeId);
          recipeDetailMetaRepository.save(recipeDetailMeta);
        }

        @DisplayName("Then - 레시피 상세 메타가 저장된다")
        @Test
        void thenRecipeDetailMetaSaved() {
          Optional<RecipeDetailMeta> foundMeta = recipeDetailMetaRepository.findByRecipeId(recipeId);
          
          assertThat(foundMeta).isPresent();
          assertThat(foundMeta.get().getRecipeId()).isEqualTo(recipeId);
          assertThat(foundMeta.get().getCookTime()).isEqualTo(30);
          assertThat(foundMeta.get().getServings()).isEqualTo(2);
          assertThat(foundMeta.get().getDescription()).isEqualTo("맛있는 김치찌개 만들기");
          assertThat(foundMeta.get().getCreatedAt()).isEqualTo(createdAt);
        }
      }
    }

    @Nested
    @DisplayName("Given - 여러 레시피 상세 메타가 주어졌을 때")
    class GivenMultipleRecipeDetailMetas {

      private List<UUID> recipeIds;
      private List<RecipeDetailMeta> recipeDetailMetas;
      private LocalDateTime createdAt;

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        createdAt = LocalDateTime.now();
        doReturn(createdAt).when(clock).now();

        recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        
        recipeDetailMetas = List.of(
            RecipeDetailMeta.create(30, 2, "김치찌개", clock, recipeIds.get(0)),
            RecipeDetailMeta.create(15, 1, "계란찜", clock, recipeIds.get(1)),
            RecipeDetailMeta.create(45, 4, "불고기", clock, recipeIds.get(2))
        );

        recipeDetailMetaRepository.saveAll(recipeDetailMetas);
      }

      @DisplayName("When - 여러 레시피 ID로 상세 메타를 조회하면")
      @Nested
      class WhenFindMetasByMultipleRecipeIds {

        private List<RecipeDetailMeta> foundMetas;

        @BeforeEach
        void setUp() {
          foundMetas = recipeDetailMetaRepository.findAllByRecipeIdIn(recipeIds);
        }

        @DisplayName("Then - 해당 레시피 상세 메타들이 조회된다")
        @Test
        void thenRecipeDetailMetasAreFound() {
          assertThat(foundMetas).hasSize(3);
          assertThat(foundMetas).extracting("description")
              .containsExactlyInAnyOrder("김치찌개", "계란찜", "불고기");
          assertThat(foundMetas).extracting("cookTime")
              .containsExactlyInAnyOrder(30, 15, 45);
          assertThat(foundMetas).extracting("servings")
              .containsExactlyInAnyOrder(2, 1, 4);
          assertThat(foundMetas).extracting("createdAt").containsOnly(createdAt);
          
          foundMetas.forEach(meta -> {
            assertThat(meta.getRecipeId()).isIn(recipeIds);
          });
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 상세 메타 조회")
  class FindRecipeDetailMeta {

    @Nested
    @DisplayName("Given - 저장된 레시피 상세 메타가 있을 때")
    class GivenSavedRecipeDetailMeta {
      private UUID recipeId;
      private RecipeDetailMeta savedMeta;
      private LocalDateTime createdAt;

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        recipeId = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        doReturn(createdAt).when(clock).now();

        savedMeta = RecipeDetailMeta.create(25, 3, "된장찌개", clock, recipeId);
        recipeDetailMetaRepository.save(savedMeta);
      }

      @DisplayName("When - 레시피 ID로 상세 메타를 조회하면")
      @Nested
      class WhenFindMetaByRecipeId {

        private Optional<RecipeDetailMeta> foundMeta;

        @BeforeEach
        void setUp() {
          foundMeta = recipeDetailMetaRepository.findByRecipeId(recipeId);
        }

        @DisplayName("Then - 해당 레시피 상세 메타가 조회된다")
        @Test
        void thenRecipeDetailMetaIsFound() {
          assertThat(foundMeta).isPresent();
          assertThat(foundMeta.get().getRecipeId()).isEqualTo(recipeId);
          assertThat(foundMeta.get().getCookTime()).isEqualTo(25);
          assertThat(foundMeta.get().getServings()).isEqualTo(3);
          assertThat(foundMeta.get().getDescription()).isEqualTo("된장찌개");
          assertThat(foundMeta.get().getCreatedAt()).isEqualTo(createdAt);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {
      private UUID nonExistentRecipeId;

      @BeforeEach
      void setUp() {
        nonExistentRecipeId = UUID.randomUUID();
      }

      @DisplayName("When - 존재하지 않는 레시피 ID로 조회하면")
      @Nested
      class WhenFindMetaByNonExistentRecipeId {

        private Optional<RecipeDetailMeta> foundMeta;

        @BeforeEach
        void setUp() {
          foundMeta = recipeDetailMetaRepository.findByRecipeId(nonExistentRecipeId);
        }

        @DisplayName("Then - 빈 Optional이 반환된다")
        @Test
        void thenEmptyOptionalIsReturned() {
          assertThat(foundMeta).isEmpty();
        }
      }
    }

    @Nested
    @DisplayName("Given - 일부만 존재하는 레시피 ID들이 주어졌을 때")
    class GivenPartiallyExistingRecipeIds {
      private UUID existingRecipeId1;
      private UUID existingRecipeId2;
      private UUID nonExistentRecipeId;
      private List<UUID> mixedRecipeIds;
      private LocalDateTime createdAt;

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        createdAt = LocalDateTime.now();
        doReturn(createdAt).when(clock).now();

        existingRecipeId1 = UUID.randomUUID();
        existingRecipeId2 = UUID.randomUUID();
        nonExistentRecipeId = UUID.randomUUID();
        
        mixedRecipeIds = List.of(existingRecipeId1, existingRecipeId2, nonExistentRecipeId);

        // 2개만 저장
        List<RecipeDetailMeta> existingMetas = List.of(
            RecipeDetailMeta.create(20, 2, "미역국", clock, existingRecipeId1),
            RecipeDetailMeta.create(35, 3, "갈비탕", clock, existingRecipeId2)
        );
        recipeDetailMetaRepository.saveAll(existingMetas);
      }

      @DisplayName("When - 일부만 존재하는 레시피 ID들로 조회하면")
      @Nested
      class WhenFindMetasByPartiallyExistingIds {

        private List<RecipeDetailMeta> foundMetas;

        @BeforeEach
        void setUp() {
          foundMetas = recipeDetailMetaRepository.findAllByRecipeIdIn(mixedRecipeIds);
        }

        @DisplayName("Then - 존재하는 메타들만 반환된다")
        @Test
        void thenOnlyExistingMetasAreReturned() {
          assertThat(foundMetas).hasSize(2);
          assertThat(foundMetas).extracting("description")
              .containsExactlyInAnyOrder("미역국", "갈비탕");
          assertThat(foundMetas).extracting("recipeId")
              .containsExactlyInAnyOrder(existingRecipeId1, existingRecipeId2);
          assertThat(foundMetas).extracting("recipeId")
              .doesNotContain(nonExistentRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
    class GivenEmptyRecipeIds {
      private List<UUID> emptyRecipeIds;

      @BeforeEach
      void setUp() {
        emptyRecipeIds = List.of();
      }

      @DisplayName("When - 빈 목록으로 조회하면")
      @Nested
      class WhenFindMetasByEmptyList {

        private List<RecipeDetailMeta> foundMetas;

        @BeforeEach
        void setUp() {
          foundMetas = recipeDetailMetaRepository.findAllByRecipeIdIn(emptyRecipeIds);
        }

        @DisplayName("Then - 빈 목록이 반환된다")
        @Test
        void thenEmptyListIsReturned() {
          assertThat(foundMetas).isEmpty();
        }
      }
    }
  }
}
