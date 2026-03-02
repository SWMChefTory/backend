package com.cheftory.api.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.tracking.dto.TrackingClickRequest;
import com.cheftory.api.tracking.dto.TrackingImpressionRequest;
import com.cheftory.api.tracking.entity.RecipeClick;
import com.cheftory.api.tracking.entity.RecipeImpression;
import com.cheftory.api.tracking.entity.SurfaceType;
import com.cheftory.api.tracking.repository.RecipeClickRepository;
import com.cheftory.api.tracking.repository.RecipeImpressionRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("TrackingService 테스트")
class TrackingServiceTest {

    private RecipeImpressionRepository impressionRepository;
    private RecipeClickRepository clickRepository;
    private Clock clock;
    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        impressionRepository = mock(RecipeImpressionRepository.class);
        clickRepository = mock(RecipeClickRepository.class);
        clock = mock(Clock.class);
        trackingService = new TrackingService(impressionRepository, clickRepository, clock);
    }

    @Nested
    @DisplayName("노출 저장 (saveImpressions)")
    class SaveImpressions {

        @Test
        @DisplayName("요청의 모든 노출 항목을 엔티티로 변환해 저장한다")
        @SuppressWarnings("unchecked")
        void savesMappedImpressions() {
            UUID userId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();
            long ts1 = 1_700_000_000_000L;
            long ts2 = 1_700_000_010_000L;
            LocalDateTime now = LocalDateTime.of(2026, 3, 3, 10, 0, 0);
            doReturn(now).when(clock).now();

            TrackingImpressionRequest request = new TrackingImpressionRequest(
                    requestId,
                    SurfaceType.SEARCH_RESULTS,
                    List.of(
                            new TrackingImpressionRequest.ImpressionItem(recipeId1, 0, ts1),
                            new TrackingImpressionRequest.ImpressionItem(recipeId2, 1, ts2)));

            trackingService.saveImpressions(userId, request);

            ArgumentCaptor<List<RecipeImpression>> captor = ArgumentCaptor.forClass(List.class);
            verify(impressionRepository).saveAll(captor.capture());
            List<RecipeImpression> saved = captor.getValue();

            assertThat(saved).hasSize(2);
            assertThat(saved.get(0).getUserId()).isEqualTo(userId);
            assertThat(saved.get(0).getRequestId()).isEqualTo(requestId);
            assertThat(saved.get(0).getSurfaceType()).isEqualTo(SurfaceType.SEARCH_RESULTS);
            assertThat(saved.get(0).getRecipeId()).isEqualTo(recipeId1);
            assertThat(saved.get(0).getPosition()).isEqualTo(0);
            assertThat(saved.get(0).getImpressedAt())
                    .isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts1), ZoneId.of("Asia/Seoul")));
            assertThat(saved.get(0).getCreatedAt()).isEqualTo(now);

            assertThat(saved.get(1).getUserId()).isEqualTo(userId);
            assertThat(saved.get(1).getRequestId()).isEqualTo(requestId);
            assertThat(saved.get(1).getSurfaceType()).isEqualTo(SurfaceType.SEARCH_RESULTS);
            assertThat(saved.get(1).getRecipeId()).isEqualTo(recipeId2);
            assertThat(saved.get(1).getPosition()).isEqualTo(1);
            assertThat(saved.get(1).getImpressedAt())
                    .isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts2), ZoneId.of("Asia/Seoul")));
            assertThat(saved.get(1).getCreatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("클릭 저장 (saveClick)")
    class SaveClick {

        @Test
        @DisplayName("요청 데이터를 클릭 엔티티로 변환해 저장한다")
        void savesMappedClick() {
            UUID userId = UUID.randomUUID();
            UUID requestId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            long ts = 1_700_000_020_000L;
            LocalDateTime now = LocalDateTime.of(2026, 3, 3, 10, 10, 0);
            doReturn(now).when(clock).now();

            TrackingClickRequest request =
                    new TrackingClickRequest(requestId, SurfaceType.HOME_POPULAR_RECIPES, recipeId, 3, ts);

            trackingService.saveClick(userId, request);

            ArgumentCaptor<RecipeClick> captor = ArgumentCaptor.forClass(RecipeClick.class);
            verify(clickRepository).save(captor.capture());
            RecipeClick saved = captor.getValue();

            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getRequestId()).isEqualTo(requestId);
            assertThat(saved.getSurfaceType()).isEqualTo(SurfaceType.HOME_POPULAR_RECIPES);
            assertThat(saved.getRecipeId()).isEqualTo(recipeId);
            assertThat(saved.getPosition()).isEqualTo(3);
            assertThat(saved.getClickedAt())
                    .isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("Asia/Seoul")));
            assertThat(saved.getCreatedAt()).isEqualTo(now);
        }
    }
}
