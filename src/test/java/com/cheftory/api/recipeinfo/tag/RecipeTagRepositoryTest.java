package com.cheftory.api.recipeinfo.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.tag.entity.RecipeTag;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeTag Repository")
public class RecipeTagRepositoryTest extends DbContextTest {

  @Autowired private RecipeTagRepository recipeTagRepository;

  @MockitoBean private Clock clock;

  private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

  @BeforeEach
  void setUp() {
    doReturn(FIXED_TIME).when(clock).now();
  }

  @Nested
  @DisplayName("레시피 태그 저장")
  class SaveRecipeTag {

    @Nested
    @DisplayName("Given - 유효한 레시피 태그가 주어졌을 때")
    class GivenValidRecipeTag {
      private String tag;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        tag = "한식";
        doReturn(FIXED_TIME).when(clock).now();
      }

      @Nested
      @DisplayName("When - 레시피 태그를 저장한다면")
      class WhenSavingRecipeTag {

        private RecipeTag recipeTag;

        @BeforeEach
        void beforeEach() {
          recipeTag = RecipeTag.create(tag, recipeId, clock);
          recipeTagRepository.save(recipeTag);
        }

        @DisplayName("Then - 레시피 태그가 저장되어야 한다")
        @Test
        public void thenShouldPersistRecipeTag() {
          RecipeTag savedTag = recipeTagRepository.findById(recipeTag.getId()).orElseThrow();

          assertThat(savedTag.getTag()).isEqualTo("한식");
          assertThat(savedTag.getRecipeId()).isEqualTo(recipeId);
          assertThat(savedTag.getCreatedAt()).isEqualTo(FIXED_TIME);
        }
      }
    }
  }

  @Nested
  @DisplayName("특정 레시피의 태그들 조회")
  class FindRecipeTags {
    @Nested
    @DisplayName("Given - 특정 레시피에 태그들이 존재할 때")
    class GivenRecipeWithTags {
      private UUID recipeId;
      private RecipeTag tag1;
      private RecipeTag tag2;
      private RecipeTag tag3;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        tag1 = RecipeTag.create("한식", recipeId, clock);
        tag2 = RecipeTag.create("매운맛", recipeId, clock);
        tag3 = RecipeTag.create("간단요리", recipeId, clock);
        recipeTagRepository.save(tag1);
        recipeTagRepository.save(tag2);
        recipeTagRepository.save(tag3);
      }

      @Nested
      @DisplayName("When - 해당 레시피의 태그들을 조회하면")
      class WhenFindingTagsByRecipeId {

        @DisplayName("Then - 모든 태그들이 조회되어야 한다")
        @Test
        public void thenShouldReturnAllTagsForRecipe() {
          var tags = recipeTagRepository.findAllByRecipeId(recipeId);
          assertThat(tags).hasSize(3);
          assertThat(tags)
              .extracting(RecipeTag::getTag)
              .containsExactlyInAnyOrder("한식", "매운맛", "간단요리");
        }
      }

      @Nested
      @DisplayName("When - 레시피 IDs로 태그를 조회하면")
      class WhenFindingTagsByRecipeIds {

        @DisplayName("Then - 모든 태그들이 조회되어야 한다")
        @Test
        public void thenShouldReturnAllTagsForRecipe() {
          var tags = recipeTagRepository.findAllByRecipeIdIn(java.util.List.of(recipeId));
          assertThat(tags).hasSize(3);
          assertThat(tags)
              .extracting(RecipeTag::getTag)
              .containsExactlyInAnyOrder("한식", "매운맛", "간단요리");
        }
      }
    }
  }
}
