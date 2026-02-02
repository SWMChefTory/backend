package com.cheftory.api.recipe.search;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Recipe Search Controller")
public class RecipeSearchControllerTest extends RestDocsTest {

    private RecipeSearchFacade recipeSearchFacade;
    private RecipeSearchController controller;
    private GlobalExceptionHandler exceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        recipeSearchFacade = mock(RecipeSearchFacade.class);
        controller = new RecipeSearchController(recipeSearchFacade);

        exceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(exceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();
    }

    @Nested
    @DisplayName("레시피 검색")
    class SearchRecipes {

        private UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            var authentication = new UsernamePasswordAuthenticationToken(userId, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("커서 기반 검색 결과를 반환한다")
        void shouldReturnSearchResultsWithCursor() {
            String query = "김치찌개";
            String cursor = "cursor-1";
            String nextCursor = "cursor-2";
            RecipeOverview recipeOverview = stubRecipeOverview();

            doReturn(CursorPage.of(List.of(recipeOverview), nextCursor))
                    .when(recipeSearchFacade)
                    .searchRecipes(query, userId, cursor);

            given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .param("query", query)
                    .param("cursor", cursor)
                    .get("/api/v1/recipes/search")
                    .then()
                    .status(HttpStatus.OK)
                    .body("searched_recipes", hasSize(1))
                    .body("has_next", equalTo(true))
                    .body("next_cursor", equalTo(nextCursor))
                    .apply(document(
                            getNestedClassPath(this.getClass()) + "/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            queryParameters(
                                    parameterWithName("query").description("검색어"),
                                    parameterWithName("cursor")
                                            .description("커서 기반 페이지네이션을 위한 커서 값")
                                            .optional()),
                            responseFields(
                                    fieldWithPath("searched_recipes").description("검색된 레시피 목록"),
                                    fieldWithPath("searched_recipes[].recipe_id")
                                            .description("레시피 ID"),
                                    fieldWithPath("searched_recipes[].recipe_title")
                                            .description("레시피 제목"),
                                    fieldWithPath("searched_recipes[].tags").description("레시피 태그 목록"),
                                    fieldWithPath("searched_recipes[].tags[].name")
                                            .description("태그 이름"),
                                    fieldWithPath("searched_recipes[].description")
                                            .description("레시피 설명"),
                                    fieldWithPath("searched_recipes[].servings").description("인분"),
                                    fieldWithPath("searched_recipes[].cooking_time")
                                            .description("조리 시간(분)"),
                                    fieldWithPath("searched_recipes[].video_id").description("레시피 비디오 ID"),
                                    fieldWithPath("searched_recipes[].channel_title")
                                            .description("레시피 채널명"),
                                    fieldWithPath("searched_recipes[].count").description("레시피 조회 수"),
                                    fieldWithPath("searched_recipes[].video_url")
                                            .description("레시피 비디오 URL"),
                                    fieldWithPath("searched_recipes[].video_type")
                                            .description("비디오 타입 (NORMAL 또는 SHORTS)"),
                                    fieldWithPath("searched_recipes[].video_thumbnail_url")
                                            .description("레시피 비디오 썸네일 URL"),
                                    fieldWithPath("searched_recipes[].video_seconds")
                                            .description("레시피 비디오 재생 시간"),
                                    fieldWithPath("searched_recipes[].is_viewed")
                                            .description("레시피 조회 여부"),
                                    fieldWithPath("has_next").description("다음 페이지 존재 여부"),
                                    fieldWithPath("next_cursor").description("다음 커서 값"),
                                    fieldWithPath("searched_recipes[].credit_cost")
                                            .description("레시피 크레딧 비용"))));

            verify(recipeSearchFacade).searchRecipes(query, userId, cursor);
        }
    }

    private RecipeOverview stubRecipeOverview() {
        RecipeOverview overview = mock(RecipeOverview.class);
        doReturn(UUID.randomUUID()).when(overview).getRecipeId();
        doReturn("recipe").when(overview).getVideoTitle();
        doReturn(List.of("tag")).when(overview).getTags();
        doReturn(true).when(overview).getIsViewed();
        doReturn("desc").when(overview).getDescription();
        doReturn(2).when(overview).getServings();
        doReturn(10).when(overview).getCookTime();
        doReturn("video-id").when(overview).getVideoId();
        doReturn("channel").when(overview).getChannelTitle();
        doReturn(0).when(overview).getViewCount();
        doReturn(URI.create("https://example.com/video")).when(overview).getVideoUri();
        doReturn(YoutubeMetaType.NORMAL).when(overview).getVideoType();
        doReturn(URI.create("https://example.com/thumb")).when(overview).getThumbnailUrl();
        doReturn(120).when(overview).getVideoSeconds();
        doReturn(1L).when(overview).getCreditCost();
        return overview;
    }
}
