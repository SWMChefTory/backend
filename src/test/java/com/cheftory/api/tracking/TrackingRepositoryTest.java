package com.cheftory.api.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.tracking.entity.RecipeClick;
import com.cheftory.api.tracking.entity.RecipeImpression;
import com.cheftory.api.tracking.entity.SurfaceType;
import com.cheftory.api.tracking.repository.RecipeClickJpaRepository;
import com.cheftory.api.tracking.repository.RecipeClickRepository;
import com.cheftory.api.tracking.repository.RecipeClickRepositoryImpl;
import com.cheftory.api.tracking.repository.RecipeImpressionJpaRepository;
import com.cheftory.api.tracking.repository.RecipeImpressionRepository;
import com.cheftory.api.tracking.repository.RecipeImpressionRepositoryImpl;
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

@DisplayName("TrackingRepository 테스트")
@Import({RecipeImpressionRepositoryImpl.class, RecipeClickRepositoryImpl.class})
class TrackingRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeImpressionRepository impressionRepository;

    @Autowired
    private RecipeClickRepository clickRepository;

    @Autowired
    private RecipeImpressionJpaRepository impressionJpaRepository;

    @Autowired
    private RecipeClickJpaRepository clickJpaRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.now()).thenReturn(LocalDateTime.of(2026, 3, 3, 10, 0, 0));
    }

    @Nested
    @DisplayName("노출 저장 (saveAll)")
    class SaveImpressions {

        @Test
        @DisplayName("노출 엔티티 목록을 저장한다")
        void savesImpressions() {
            UUID userId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();

            RecipeImpression first = RecipeImpression.create(
                    clock, userId, requestId, SurfaceType.SEARCH_RESULTS, recipeId1, 0, 1_700_000_000_000L);
            RecipeImpression second = RecipeImpression.create(
                    clock, userId, requestId, SurfaceType.SEARCH_RESULTS, recipeId2, 1, 1_700_000_010_000L);

            impressionRepository.saveAll(List.of(first, second));

            RecipeImpression savedFirst =
                    impressionJpaRepository.findById(first.getId()).orElseThrow();
            RecipeImpression savedSecond =
                    impressionJpaRepository.findById(second.getId()).orElseThrow();

            assertThat(savedFirst.getUserId()).isEqualTo(userId);
            assertThat(savedFirst.getRequestId()).isEqualTo(requestId);
            assertThat(savedFirst.getRecipeId()).isEqualTo(recipeId1);
            assertThat(savedFirst.getSurfaceType()).isEqualTo(SurfaceType.SEARCH_RESULTS);
            assertThat(savedFirst.getCountryCode()).isEqualTo("KR");

            assertThat(savedSecond.getUserId()).isEqualTo(userId);
            assertThat(savedSecond.getRequestId()).isEqualTo(requestId);
            assertThat(savedSecond.getRecipeId()).isEqualTo(recipeId2);
            assertThat(savedSecond.getSurfaceType()).isEqualTo(SurfaceType.SEARCH_RESULTS);
            assertThat(savedSecond.getCountryCode()).isEqualTo("KR");
        }
    }

    @Nested
    @DisplayName("클릭 저장 (save)")
    class SaveClick {

        @Test
        @DisplayName("클릭 엔티티를 저장한다")
        void savesClick() {
            UUID userId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeClick click = RecipeClick.create(
                    clock, userId, requestId, SurfaceType.HOME_POPULAR_RECIPES, recipeId, 3, 1_700_000_020_000L);

            clickRepository.save(click);

            RecipeClick saved = clickJpaRepository.findById(click.getId()).orElseThrow();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getRequestId()).isEqualTo(requestId);
            assertThat(saved.getRecipeId()).isEqualTo(recipeId);
            assertThat(saved.getSurfaceType()).isEqualTo(SurfaceType.HOME_POPULAR_RECIPES);
            assertThat(saved.getCountryCode()).isEqualTo("KR");
        }
    }
}
