package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CountIdCursor;
import com.cheftory.api._common.cursor.CountIdCursorCodec;
import com.cheftory.api._common.cursor.CursorErrorCode;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepository;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepositoryImpl;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
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
@Import({RecipeInfoRepositoryImpl.class, CountIdCursorCodec.class})
@DisplayName("RecipeInfoRepository 테스트")
class RecipeInfoRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeInfoRepository recipeInfoRepository;

    @Autowired
    private CountIdCursorCodec countIdCursorCodec;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("get 메서드는")
    class DescribeGet {

        @Test
        @DisplayName("데이터가 존재하면 레시피 정보를 반환한다")
        void shouldReturnRecipeInfoWhenExists() throws RecipeInfoException {
            RecipeInfo created = RecipeInfo.create(clock);
            recipeInfoRepository.create(created);

            RecipeInfo result = recipeInfoRepository.get(idOf(created));

            assertThat(result).isNotNull();
            assertThat(idOf(result)).isEqualTo(idOf(created));
            assertThat(statusOf(result)).isEqualTo(statusOf(created));
        }

        @Test
        @DisplayName("데이터가 없으면 RECIPE_INFO_NOT_FOUND 예외를 던진다")
        void shouldThrowWhenNotExists() {
            assertThatThrownBy(() -> recipeInfoRepository.get(UUID.randomUUID()))
                    .isInstanceOf(RecipeInfoException.class)
                    .extracting("error")
                    .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("목록 조회 메서드는")
    class DescribeListQueries {

        @Test
        @DisplayName("gets는 SUCCESS 상태의 레시피만 반환한다")
        void shouldReturnOnlySuccessRecipesForGets() {
            RecipeInfo success = recipeInfoRepository.create(RecipeInfo.create(clock));
            RecipeInfo failed = recipeInfoRepository.create(RecipeInfo.create(clock));
            RecipeInfo progress = recipeInfoRepository.create(RecipeInfo.create(clock));

            success.success(clock);
            recipeInfoRepository.create(success);

            failed.failed(clock);
            recipeInfoRepository.create(failed);

            List<UUID> ids = List.of(idOf(success), idOf(failed), idOf(progress));
            List<RecipeInfo> result = recipeInfoRepository.gets(ids);

            assertThat(result).hasSize(1);
            assertThat(idOf(result.getFirst())).isEqualTo(idOf(success));
        }

        @Test
        @DisplayName("getProgressRecipes는 IN_PROGRESS 상태만 반환한다")
        void shouldReturnOnlyProgressRecipes() {
            RecipeInfo success = recipeInfoRepository.create(RecipeInfo.create(clock));
            RecipeInfo failed = recipeInfoRepository.create(RecipeInfo.create(clock));
            RecipeInfo progress = recipeInfoRepository.create(RecipeInfo.create(clock));

            success.success(clock);
            recipeInfoRepository.create(success);

            failed.failed(clock);
            recipeInfoRepository.create(failed);

            List<UUID> ids = List.of(idOf(success), idOf(failed), idOf(progress));
            List<RecipeInfo> result = recipeInfoRepository.getProgressRecipes(ids);

            assertThat(result).hasSize(1);
            assertThat(idOf(result.getFirst())).isEqualTo(idOf(progress));
        }
    }

    @Nested
    @DisplayName("상태 변경 메서드는")
    class DescribeStateTransitions {

        @Test
        @DisplayName("success는 레시피 상태를 SUCCESS로 변경한다")
        void shouldMarkSuccess() throws RecipeInfoException {
            RecipeInfo created = recipeInfoRepository.create(RecipeInfo.create(clock));

            RecipeInfo updated = recipeInfoRepository.success(idOf(created), clock);

            assertThat(updated.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("failed는 레시피 상태를 FAILED로 변경한다")
        void shouldMarkFailed() throws RecipeInfoException {
            RecipeInfo created = recipeInfoRepository.create(RecipeInfo.create(clock));

            RecipeInfo updated = recipeInfoRepository.failed(idOf(created), clock);

            assertThat(updated.isFailed()).isTrue();
        }

        @Test
        @DisplayName("block은 레시피 상태를 BLOCKED로 변경한다")
        void shouldMarkBlocked() throws RecipeInfoException {
            RecipeInfo created = recipeInfoRepository.create(RecipeInfo.create(clock));

            recipeInfoRepository.block(idOf(created), clock);

            RecipeInfo blocked = recipeInfoRepository.get(idOf(created));
            assertThat(blocked.isBlocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("기본 조작 메서드는")
    class DescribeBasicOperations {

        @Test
        @DisplayName("create는 레시피를 저장한다")
        void shouldPersistCreatedRecipe() throws RecipeInfoException {
            RecipeInfo created = RecipeInfo.create(clock);

            RecipeInfo saved = recipeInfoRepository.create(created);
            RecipeInfo found = recipeInfoRepository.get(idOf(saved));

            assertThat(idOf(found)).isEqualTo(idOf(saved));
            assertThat(statusOf(found)).isEqualTo(RecipeStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("increaseCount는 조회수를 증가시킨다")
        void shouldIncreaseViewCount() throws RecipeInfoException {
            RecipeInfo created = recipeInfoRepository.create(RecipeInfo.create(clock));

            recipeInfoRepository.increaseCount(created.getId());

            RecipeInfo updated = recipeInfoRepository.get(created.getId());

            assertThat(viewCountOf(updated)).isEqualTo(1);
        }

        @Test
        @DisplayName("exists는 존재 여부를 반환한다")
        void shouldReturnExistence() {
            RecipeInfo created = recipeInfoRepository.create(RecipeInfo.create(clock));

            assertThat(recipeInfoRepository.exists(idOf(created))).isTrue();
            assertThat(recipeInfoRepository.exists(UUID.randomUUID())).isFalse();
        }
    }

    @Nested
    @DisplayName("인기 레시피 커서 조회 메서드는")
    class DescribePopularCursorQueries {

        @Test
        @DisplayName("popularFirst(ALL)은 SUCCESS 레시피만 조회수 내림차순으로 반환한다")
        void shouldReturnPopularFirstAll() throws RecipeInfoException {
            RecipeInfo high = recipeInfoRepository.create(RecipeInfo.create(clock));
            recipeInfoRepository.success(idOf(high), clock);
            recipeInfoRepository.increaseCount(idOf(high));
            recipeInfoRepository.increaseCount(idOf(high));

            RecipeInfo low = recipeInfoRepository.create(RecipeInfo.create(clock));
            recipeInfoRepository.success(idOf(low), clock);
            recipeInfoRepository.increaseCount(idOf(low));

            CursorPage<RecipeInfo> result = recipeInfoRepository.popularFirst(RecipeInfoVideoQuery.ALL);

            List<UUID> ids = result.items().stream()
                    .map(RecipeInfoRepositoryTest.this::idOf)
                    .toList();
            assertThat(ids).contains(idOf(high), idOf(low));
            assertThat(ids.indexOf(idOf(high))).isLessThan(ids.indexOf(idOf(low)));
        }

        @Test
        @DisplayName("popularKeyset은 유효한 cursor로 다음 페이지를 조회한다")
        void shouldReturnPopularKeysetPage() throws Exception {
            RecipeInfo first = recipeInfoRepository.create(RecipeInfo.create(clock));
            recipeInfoRepository.success(idOf(first), clock);
            recipeInfoRepository.increaseCount(idOf(first));
            recipeInfoRepository.increaseCount(idOf(first));

            RecipeInfo second = recipeInfoRepository.create(RecipeInfo.create(clock));
            recipeInfoRepository.success(idOf(second), clock);
            recipeInfoRepository.increaseCount(idOf(second));

            String cursor = countIdCursorCodec.encode(new CountIdCursor(2, idOf(first)));
            CursorPage<RecipeInfo> result = recipeInfoRepository.popularKeyset(RecipeInfoVideoQuery.ALL, cursor);

            List<UUID> ids = result.items().stream()
                    .map(RecipeInfoRepositoryTest.this::idOf)
                    .toList();
            assertThat(ids).contains(idOf(second));
            assertThat(ids).doesNotContain(idOf(first));
        }

        @Test
        @DisplayName("popularKeyset은 잘못된 cursor면 INVALID_CURSOR 예외를 던진다")
        void shouldThrowWhenPopularCursorInvalid() {
            assertThatThrownBy(() -> recipeInfoRepository.popularKeyset(RecipeInfoVideoQuery.ALL, "invalid-cursor"))
                    .isInstanceOf(CursorException.class)
                    .extracting("error")
                    .isEqualTo(CursorErrorCode.INVALID_CURSOR);
        }
    }

    private UUID idOf(RecipeInfo recipeInfo) {
        return (UUID) getField(recipeInfo, "id");
    }

    private long viewCountOf(RecipeInfo recipeInfo) {
        return (Integer) getField(recipeInfo, "viewCount");
    }

    private RecipeStatus statusOf(RecipeInfo recipeInfo) {
        return (RecipeStatus) getField(recipeInfo, "recipeStatus");
    }
}
