package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepository;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeInfoService")
class RecipeInfoServiceTest {

    private RecipeInfoRepository repository;
    private Clock clock;
    private I18nTranslator i18nTranslator;
    private RecipeInfoService service;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeInfoRepository.class);
        clock = mock(Clock.class);
        i18nTranslator = mock(I18nTranslator.class);
        service = mock(RecipeInfoService.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        setField(service, "repository", repository);
        setField(service, "clock", clock);
        setField(service, "i18nTranslator", i18nTranslator);
    }

    @Nested
    @DisplayName("getSuccess(recipeId)")
    class GetSuccess {

        @Test
        @DisplayName("성공 상태의 레시피면 레시피를 반환한다")
        void shouldReturnSuccessRecipe() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            doReturn(false).when(recipeInfo).isFailed();
            doReturn(false).when(recipeInfo).isBlocked();
            doReturn(recipeInfo).when(repository).get(recipeId);

            RecipeInfo result = service.getSuccess(recipeId);

            assertThat(result).isEqualTo(recipeInfo);
            verify(repository).get(recipeId);
        }

        @Test
        @DisplayName("실패 상태 레시피면 RECIPE_FAILED 예외를 던진다")
        void shouldThrowRecipeFailedWhenFailed() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            doReturn(true).when(recipeInfo).isFailed();
            doReturn(recipeInfo).when(repository).get(recipeId);

            assertThatThrownBy(() -> service.getSuccess(recipeId))
                    .isInstanceOf(RecipeInfoException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeInfoErrorCode.RECIPE_FAILED);
        }

        @Test
        @DisplayName("차단 상태 레시피면 RECIPE_BANNED 예외를 던진다")
        void shouldThrowRecipeBannedWhenBlocked() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            doReturn(false).when(recipeInfo).isFailed();
            doReturn(true).when(recipeInfo).isBlocked();
            doReturn(recipeInfo).when(repository).get(recipeId);

            assertThatThrownBy(() -> service.getSuccess(recipeId))
                    .isInstanceOf(RecipeInfoException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeInfoErrorCode.RECIPE_BANNED);
        }
    }

    @Nested
    @DisplayName("delegate methods")
    class DelegateMethods {

        @Test
        @DisplayName("increaseCount는 repository로 위임한다")
        void shouldDelegateIncreaseCount() {
            UUID recipeId = UUID.randomUUID();

            service.increaseCount(recipeId);

            verify(repository).increaseCount(recipeId);
        }

        @Test
        @DisplayName("create는 신규 레시피를 생성해 repository.create를 호출한다")
        void shouldCreateRecipe() {
            RecipeInfo created = service.create();

            assertThat(created).isNotNull();
            verify(repository).create(created);
        }

        @Test
        @DisplayName("getProgresses는 repository.getProgressRecipes를 호출한다")
        void shouldDelegateGetProgresses() {
            List<UUID> recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<RecipeInfo> expected = List.of(mock(RecipeInfo.class));
            doReturn(expected).when(repository).getProgressRecipes(recipeIds);

            List<RecipeInfo> result = service.getProgresses(recipeIds);

            assertThat(result).isEqualTo(expected);
            verify(repository).getProgressRecipes(recipeIds);
        }

        @Test
        @DisplayName("gets는 repository.gets를 호출한다")
        void shouldDelegateGets() {
            List<UUID> recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<RecipeInfo> expected = List.of(mock(RecipeInfo.class));
            doReturn(expected).when(repository).gets(recipeIds);

            List<RecipeInfo> result = service.gets(recipeIds);

            assertThat(result).isEqualTo(expected);
            verify(repository).gets(recipeIds);
        }

        @Test
        @DisplayName("success는 repository.success를 호출한다")
        void shouldDelegateSuccess() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo expected = mock(RecipeInfo.class);
            doReturn(expected).when(repository).success(recipeId, clock);

            RecipeInfo result = service.success(recipeId);

            assertThat(result).isEqualTo(expected);
            verify(repository).success(recipeId, clock);
        }

        @Test
        @DisplayName("failed는 repository.failed를 호출한다")
        void shouldDelegateFailed() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo expected = mock(RecipeInfo.class);
            doReturn(expected).when(repository).failed(recipeId, clock);

            RecipeInfo result = service.failed(recipeId);

            assertThat(result).isEqualTo(expected);
            verify(repository).failed(recipeId, clock);
        }

        @Test
        @DisplayName("block은 repository.block을 호출한다")
        void shouldDelegateBlock() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();

            service.block(recipeId);

            verify(repository).block(recipeId, clock);
        }

        @Test
        @DisplayName("exists는 repository.exists를 호출한다")
        void shouldDelegateExists() {
            UUID recipeId = UUID.randomUUID();
            doReturn(true).when(repository).exists(recipeId);

            boolean result = service.exists(recipeId);

            assertThat(result).isTrue();
            verify(repository).exists(recipeId);
        }

        @Test
        @DisplayName("get은 repository.get을 호출한다")
        void shouldDelegateGet() throws RecipeInfoException {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo expected = mock(RecipeInfo.class);
            doReturn(expected).when(repository).get(recipeId);

            RecipeInfo result = service.get(recipeId);

            assertThat(result).isEqualTo(expected);
            verify(repository).get(recipeId);
        }
    }

    @Nested
    @DisplayName("cursor queries")
    class CursorQueries {

        @Test
        @DisplayName("인기 레시피는 cursor가 없으면 popularFirst를 호출한다")
        void shouldCallPopularFirstWhenCursorBlank() throws Exception {
            CursorPage<RecipeInfo> expected = CursorPage.of(List.of(), "next");
            doReturn(expected).when(repository).popularFirst(RecipeInfoVideoQuery.ALL);

            CursorPage<RecipeInfo> result = service.getPopulars("", RecipeInfoVideoQuery.ALL);

            assertThat(result).isEqualTo(expected);
            verify(repository).popularFirst(RecipeInfoVideoQuery.ALL);
        }

        @Test
        @DisplayName("인기 레시피는 cursor가 있으면 popularKeyset을 호출한다")
        void shouldCallPopularKeysetWhenCursorExists() throws Exception {
            CursorPage<RecipeInfo> expected = CursorPage.of(List.of(), null);
            doReturn(expected).when(repository).popularKeyset(RecipeInfoVideoQuery.SHORTS, "cursor");

            CursorPage<RecipeInfo> result = service.getPopulars("cursor", RecipeInfoVideoQuery.SHORTS);

            assertThat(result).isEqualTo(expected);
            verify(repository).popularKeyset(RecipeInfoVideoQuery.SHORTS, "cursor");
        }

        @Test
        @DisplayName("음식 종류는 cursor가 없으면 cusineFirst를 호출한다")
        void shouldCallCuisineFirstWhenCursorBlank() throws Exception {
            CursorPage<RecipeInfo> expected = CursorPage.of(List.of(), "next");
            doReturn("한식").when(i18nTranslator).translate(RecipeCuisineType.KOREAN.messageKey());
            doReturn(expected).when(repository).cusineFirst("한식");

            CursorPage<RecipeInfo> result = service.getCuisines(RecipeCuisineType.KOREAN, null);

            assertThat(result).isEqualTo(expected);
            verify(i18nTranslator).translate(RecipeCuisineType.KOREAN.messageKey());
            verify(repository).cusineFirst("한식");
        }

        @Test
        @DisplayName("음식 종류는 cursor가 있으면 cuisineKeyset을 호출한다")
        void shouldCallCuisineKeysetWhenCursorExists() throws Exception {
            CursorPage<RecipeInfo> expected = CursorPage.of(List.of(), null);
            doReturn("한식").when(i18nTranslator).translate(RecipeCuisineType.KOREAN.messageKey());
            doReturn(expected).when(repository).cuisineKeyset("한식", "cursor");

            CursorPage<RecipeInfo> result = service.getCuisines(RecipeCuisineType.KOREAN, "cursor");

            assertThat(result).isEqualTo(expected);
            verify(i18nTranslator).translate(RecipeCuisineType.KOREAN.messageKey());
            verify(repository).cuisineKeyset("한식", "cursor");
        }
    }
}
