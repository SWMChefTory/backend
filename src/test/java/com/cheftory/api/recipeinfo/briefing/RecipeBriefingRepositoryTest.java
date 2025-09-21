package com.cheftory.api.recipeinfo.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeBriefingRepository 테스트")
public class RecipeBriefingRepositoryTest extends DbContextTest {

  @Autowired private RecipeBriefingRepository recipeBriefingRepository;

  @MockitoBean private Clock clock;

  private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

  @BeforeEach
  void setUp() {
    doReturn(FIXED_TIME).when(clock).now();
  }

  @Nested
  @DisplayName("레시피 ID로 브리핑 목록 조회")
  class FindAllByRecipeId {

    @Nested
    @DisplayName("Given - 특정 레시피의 브리핑들이 저장되어 있을 때")
    class GivenRecipeBriefingsExist {

      private UUID recipeId;
      private UUID anotherRecipeId;
      private List<String> briefingContents;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        anotherRecipeId = UUID.randomUUID();
        briefingContents = List.of("이 요리는 매우 맛있습니다", "조리 시간이 30분 정도 걸립니다", "초보자도 쉽게 따라할 수 있어요");

        briefingContents.forEach(
            content -> {
              RecipeBriefing briefing = RecipeBriefing.create(recipeId, content, clock);
              recipeBriefingRepository.save(briefing);
            });

        RecipeBriefing anotherRecipeBriefing =
            RecipeBriefing.create(anotherRecipeId, "다른 레시피의 브리핑", clock);
        recipeBriefingRepository.save(anotherRecipeBriefing);
      }

      @Nested
      @DisplayName("When - 특정 레시피 ID로 브리핑 목록을 조회하면")
      class WhenFindAllByRecipeId {

        private List<RecipeBriefing> results;

        @BeforeEach
        void setUp() {
          results = recipeBriefingRepository.findAllByRecipeId(recipeId);
        }

        @DisplayName("Then - 해당 레시피의 브리핑들만 반환된다")
        @Test
        void shouldReturnOnlyTargetRecipeBriefings() {
          assertThat(results).hasSize(3);

          results.forEach(
              briefing -> {
                assertThat(briefing.getRecipeId()).isEqualTo(recipeId);
                assertThat(briefing.getCreatedAt()).isEqualTo(FIXED_TIME);
              });

          List<String> actualContents = results.stream().map(RecipeBriefing::getContent).toList();

          assertThat(actualContents).containsExactlyInAnyOrderElementsOf(briefingContents);
        }

        @DisplayName("Then - 다른 레시피의 브리핑은 포함되지 않는다")
        @Test
        void shouldNotIncludeOtherRecipeBriefings() {
          List<UUID> resultRecipeIds =
              results.stream().map(RecipeBriefing::getRecipeId).distinct().toList();

          assertThat(resultRecipeIds).hasSize(1);
          assertThat(resultRecipeIds.get(0)).isEqualTo(recipeId);
          assertThat(resultRecipeIds).doesNotContain(anotherRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 브리핑이 없는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {

      private UUID nonExistentRecipeId;

      @BeforeEach
      void setUp() {
        nonExistentRecipeId = UUID.randomUUID();

        UUID anotherRecipeId = UUID.randomUUID();
        RecipeBriefing anotherBriefing =
            RecipeBriefing.create(anotherRecipeId, "다른 레시피의 브리핑", clock);
        recipeBriefingRepository.save(anotherBriefing);
      }

      @Nested
      @DisplayName("When - 존재하지 않는 레시피 ID로 조회하면")
      class WhenFindAllByNonExistentRecipeId {

        @DisplayName("Then - 빈 목록이 반환된다")
        @Test
        void shouldReturnEmptyList() {
          List<RecipeBriefing> results =
              recipeBriefingRepository.findAllByRecipeId(nonExistentRecipeId);

          assertThat(results).isEmpty();
        }
      }
    }

    @Nested
    @DisplayName("Given - 하나의 레시피에 여러 시점의 브리핑이 있을 때")
    class GivenMultipleBriefingsAtDifferentTimes {

      private UUID recipeId;
      private LocalDateTime firstTime;
      private LocalDateTime secondTime;
      private LocalDateTime thirdTime;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        firstTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        secondTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        thirdTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        doReturn(firstTime).when(clock).now();
        RecipeBriefing firstBriefing = RecipeBriefing.create(recipeId, "첫 번째 브리핑", clock);
        recipeBriefingRepository.save(firstBriefing);

        doReturn(secondTime).when(clock).now();
        RecipeBriefing secondBriefing = RecipeBriefing.create(recipeId, "두 번째 브리핑", clock);
        recipeBriefingRepository.save(secondBriefing);

        doReturn(thirdTime).when(clock).now();
        RecipeBriefing thirdBriefing = RecipeBriefing.create(recipeId, "세 번째 브리핑", clock);
        recipeBriefingRepository.save(thirdBriefing);
      }

      @Nested
      @DisplayName("When - 레시피 ID로 브리핑 목록을 조회하면")
      class WhenFindAllByRecipeId {

        @DisplayName("Then - 모든 브리핑이 반환된다")
        @Test
        void shouldReturnAllBriefings() {
          List<RecipeBriefing> results = recipeBriefingRepository.findAllByRecipeId(recipeId);

          assertThat(results).hasSize(3);

          List<String> contents = results.stream().map(RecipeBriefing::getContent).toList();

          assertThat(contents).containsExactlyInAnyOrder("첫 번째 브리핑", "두 번째 브리핑", "세 번째 브리핑");

          List<LocalDateTime> createdTimes =
              results.stream().map(RecipeBriefing::getCreatedAt).toList();

          assertThat(createdTimes).containsExactlyInAnyOrder(firstTime, secondTime, thirdTime);
        }
      }
    }
  }

  @Nested
  @DisplayName("브리핑 저장")
  class SaveRecipeBriefing {

    @Nested
    @DisplayName("Given - 유효한 브리핑 정보가 주어졌을 때")
    class GivenValidBriefingInfo {

      private UUID recipeId;
      private String content;
      private RecipeBriefing briefing;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        content = "정말 맛있는 요리입니다!";
        briefing = RecipeBriefing.create(recipeId, content, clock);
      }

      @Nested
      @DisplayName("When - 브리핑을 저장하면")
      class WhenSaveBriefing {

        @DisplayName("Then - 브리핑이 성공적으로 저장된다")
        @Test
        void shouldSaveBriefingSuccessfully() {
          RecipeBriefing savedBriefing = recipeBriefingRepository.save(briefing);

          assertThat(savedBriefing).isNotNull();
          assertThat(savedBriefing.getId()).isEqualTo(briefing.getId());
          assertThat(savedBriefing.getRecipeId()).isEqualTo(recipeId);
          assertThat(savedBriefing.getContent()).isEqualTo(content);
          assertThat(savedBriefing.getCreatedAt()).isEqualTo(FIXED_TIME);

          List<RecipeBriefing> found = recipeBriefingRepository.findAllByRecipeId(recipeId);
          assertThat(found).hasSize(1);
          assertThat(found.get(0)).usingRecursiveComparison().isEqualTo(savedBriefing);
        }
      }
    }
  }
}
