package com.cheftory.api.recipe.content.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.briefing.respotiory.RecipeBriefingRepository;
import com.cheftory.api.recipe.content.briefing.respotiory.RecipeBriefingRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({RecipeBriefingRepositoryImpl.class})
@DisplayName("RecipeBriefingRepository 테스트")
class RecipeBriefingRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeBriefingRepository recipeBriefingRepository;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("findAllByRecipeId 메서드는")
    class Describe_findAllByRecipeId {

        @Test
        @DisplayName("특정 레시피의 모든 브리핑을 조회한다")
        void it_returns_all_briefings_for_recipe() {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeBriefing briefing1 = RecipeBriefing.create(recipeId, "Content 1", clock);
            RecipeBriefing briefing2 = RecipeBriefing.create(recipeId, "Content 2", clock);
            recipeBriefingRepository.saveAll(List.of(briefing1, briefing2));

            // When
            List<RecipeBriefing> results = recipeBriefingRepository.findAllByRecipeId(recipeId);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results)
                    .extracting(RecipeBriefing::getContent)
                    .containsExactlyInAnyOrder("Content 1", "Content 2");
        }

        @Test
        @DisplayName("브리핑이 없는 경우 빈 리스트를 반환한다")
        void it_returns_empty_list_when_no_briefings() {
            // When
            List<RecipeBriefing> results = recipeBriefingRepository.findAllByRecipeId(UUID.randomUUID());

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("saveAll 메서드는")
    class Describe_saveAll {

        @Test
        @DisplayName("여러 브리핑을 한 번에 저장한다")
        void it_saves_multiple_briefings() {
            // Given
            UUID recipeId = UUID.randomUUID();
            RecipeBriefing briefing1 = RecipeBriefing.create(recipeId, "New Content 1", clock);
            RecipeBriefing briefing2 = RecipeBriefing.create(recipeId, "New Content 2", clock);

            // When
            recipeBriefingRepository.saveAll(List.of(briefing1, briefing2));

            // Then
            List<RecipeBriefing> results = recipeBriefingRepository.findAllByRecipeId(recipeId);
            assertThat(results).hasSize(2);
        }
    }
}
