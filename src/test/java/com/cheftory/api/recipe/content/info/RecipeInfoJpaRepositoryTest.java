package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeSourceType;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@DisplayName("RecipeInfoJpaRepository 테스트")
class RecipeInfoJpaRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeInfoJpaRepository repository;

    @Nested
    @DisplayName("조건부 상태+jobId 갱신 (updateStatusAndJobIdIfStatus)")
    class UpdateStatusAndJobIdIfStatus {
        @Test
        @DisplayName("현재 상태가 일치하면 상태와 currentJobId를 갱신하고 1을 반환한다")
        void updatesWhenStatusMatches() {
            RecipeInfo recipe = repository.save(newRecipe());
            UUID newJobId = UUID.randomUUID();

            int updated = repository.updateStatusAndJobIdIfStatus(
                    recipe.getId(), RecipeStatus.IN_PROGRESS, RecipeStatus.FAILED, newJobId);

            RecipeInfo reloaded = repository.findById(recipe.getId()).orElseThrow();
            assertThat(updated).isEqualTo(1);
            assertThat(reloaded.getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
            assertThat(reloaded.getCurrentJobId()).isEqualTo(newJobId);
        }

        @Test
        @DisplayName("현재 상태가 다르면 갱신하지 않고 0을 반환한다")
        void returnsZeroWhenStatusNotMatches() {
            RecipeInfo recipe = repository.save(newRecipe());

            int updated = repository.updateStatusAndJobIdIfStatus(
                    recipe.getId(), RecipeStatus.FAILED, RecipeStatus.IN_PROGRESS, UUID.randomUUID());

            RecipeInfo reloaded = repository.findById(recipe.getId()).orElseThrow();
            assertThat(updated).isZero();
            assertThat(reloaded.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("상태 기준 슬라이스 조회 (findByRecipeStatus)")
    class FindByRecipeStatus {
        @Test
        @DisplayName("요청한 상태의 레시피만 슬라이스로 반환한다")
        void returnsSliceByStatus() {
            RecipeInfo success1 = repository.save(newRecipe());
            RecipeInfo success2 = repository.save(newRecipe());
            RecipeInfo failed = repository.save(newRecipe());

            success1.success(fixedClock());
            success2.success(fixedClock());
            failed.failed(fixedClock());
            repository.saveAll(List.of(success1, success2, failed));

            Slice<RecipeInfo> result = repository.findByRecipeStatus(RecipeStatus.SUCCESS, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(RecipeInfo::getRecipeStatus)
                    .containsOnly(RecipeStatus.SUCCESS);
        }
    }

    private RecipeInfo newRecipe() {
        return RecipeInfo.create(fixedClock(), RecipeSourceType.YOUTUBE, "video-" + UUID.randomUUID());
    }

    private Clock fixedClock() {
        Clock clock = mock(Clock.class);
        doReturn(LocalDateTime.of(2026, 1, 1, 12, 0)).when(clock).now();
        return clock;
    }
}
