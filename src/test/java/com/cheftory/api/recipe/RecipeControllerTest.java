package com.cheftory.api.recipe;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecentRecipeOverview;
import com.cheftory.api.recipe.model.RecipeOverview;
import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.utils.RestDocsTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import com.cheftory.api.exception.GlobalExceptionHandler;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

@DisplayName("Recipe Controller")
public class RecipeControllerTest extends RestDocsTest {

    private RecipeService recipeService;
    private RecipeController controller;

    @BeforeEach
    void setUp() {
        recipeService = mock(RecipeService.class);
        controller = new RecipeController(recipeService);

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(new GlobalExceptionHandler())
                .withArgumentResolver(new UserArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = RecipeControllerTestData.userId;
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 레시피 최근 기록을 조회할 때")
      class WhenRequestingRecentRecipes {

        private List<RecentRecipeOverview> recentRecipeOverviews;

        @BeforeEach
        void setUp() {
          recentRecipeOverviews = List.of(RecipeControllerTestData.recentRecipeOverview);
          when(recipeService.findRecents(userId)).thenReturn(recentRecipeOverviews);
        }

        @Test
        @DisplayName("Then - 레시피 최근 기록을 성공적으로 반환한다")
        void thenShouldReturnRecentRecipes() {
          var response = given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .get("/api/v1/recipes/recent")
              .then()
              .status(HttpStatus.OK)
              .body("recent_recipes", hasSize(recentRecipeOverviews.size()))
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  responseFields(
                      fieldWithPath("recent_recipes").description("사용자의 최근 레시피 접근 기록"),
                      fieldWithPath("recent_recipes[].viewed_at").description("레시피 접근 시간"),
                      fieldWithPath("recent_recipes[].last_play_seconds").description(
                          "레시피 마지막 재생 시간"),
                      fieldWithPath("recent_recipes[].recipe_id").description("레시피 ID"),
                      fieldWithPath("recent_recipes[].video_id").description("레시피 비디오 ID"),
                      fieldWithPath("recent_recipes[].recipe_title").description("레시피 비디오 제목"),
                      fieldWithPath("recent_recipes[].video_thumbnail_url").description(
                          "레시피 비디오 썸네일 URL")
                  )
              ));
          verify(recipeService).findRecents(userId);

          var responseBody = response.extract().jsonPath();
          var recentRecipes = responseBody.getList("recent_recipes");

          assertThat(recentRecipes).hasSize(recentRecipeOverviews.size());

          recentRecipeOverviews.forEach(recentRecipeOverview -> {
            var actualPath =
                "recent_recipes[" + recentRecipeOverviews.indexOf(recentRecipeOverview) + "]";

            assertThat(responseBody.getUUID(actualPath + ".recipe_id"))
                .isEqualTo(recentRecipeOverview.getRecipeOverview().getId());

            assertThat(LocalDateTime.parse(responseBody.getString(actualPath + ".viewed_at")))
                .isEqualTo(recentRecipeOverview.getRecipeViewStatusInfo().getViewedAt());

            assertThat(responseBody.getString(actualPath + ".recipe_title"))
                .isEqualTo(recentRecipeOverview.getRecipeOverview().getVideoInfo().getTitle());

            assertThat(responseBody.getString(actualPath + ".video_id"))
                .isEqualTo(recentRecipeOverview.getRecipeOverview().getVideoInfo().getVideoId());

            assertThat(responseBody.getString(actualPath + ".video_thumbnail_url"))
                .isEqualTo(
                    recentRecipeOverview.getRecipeOverview().getVideoInfo().getThumbnailUrl().toString());

            assertThat(responseBody.getInt(actualPath + ".last_play_seconds"))
                .isEqualTo(recentRecipeOverview.getRecipeViewStatusInfo().getLastPlaySeconds());
          });
        }

        @Nested
        @DisplayName("When - 레시피 전체 정보를 조회할 때")
        class WhenRequestingRecipeDetail {

          private FullRecipeInfo fullRecipeInfo;
          private UUID recipeId;

          @BeforeEach
          void setUp() {
            fullRecipeInfo = RecipeControllerTestData.fullRecipeInfo;
            recipeId = RecipeControllerTestData.recipeId;
            when(recipeService.findFullRecipe(recipeId, userId)).thenReturn(fullRecipeInfo);
          }

          @Test
          @DisplayName("Then - 레시피 전체 정보를 성공적으로 반환한다")
          void thenShouldReturnRecipeDetail() {
            var response = given()
                .contentType(ContentType.JSON)
                .attribute("userId", userId.toString())
                .header("Authorization", "Bearer accessToken")
                .get("/api/v1/recipes/{recipe_id}", recipeId)
                .then()
                .status(HttpStatus.OK)
                .apply(document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestAccessTokenFields(),
                    pathParameters(
                        parameterWithName("recipe_id").description("조회할 레시피 ID")
                    ),
                    responseFields(
                        enumFields("recipe_status", "레시피의 현재 상태 :", RecipeStatus.class),
                        fieldWithPath("video_info").description("레시피 비디오 정보"),
                        fieldWithPath("ingredients_info").description("레시피 재료 정보"),
                        fieldWithPath("recipe_steps").description("레시피 단계 정보"),
                        fieldWithPath("view_status").description("레시피 시청 상태 정보"),
                        fieldWithPath("video_info.video_id").description("레시피 비디오 ID"),
                        fieldWithPath("video_info.video_title").description("레시피 비디오 제목"),
                        fieldWithPath("video_info.video_thumbnail_url").description(
                            "레시피 비디오 썸네일 URL"),
                        fieldWithPath("video_info.video_seconds").description("레시피 비디오 재생 시간"),
                        fieldWithPath("ingredients_info.id").description("레시피 재료 ID"),
                        fieldWithPath("ingredients_info.ingredients").description("레시피 재료 정보"),
                        fieldWithPath("recipe_steps[].id").description("레시피 단계 ID"),
                        fieldWithPath("recipe_steps[].step_order").description("레시피 단계 순서"),
                        fieldWithPath("recipe_steps[].subtitle").description("레시피 단계 제목"),
                        fieldWithPath("recipe_steps[].details").description("레시피 단계 설명"),
                        fieldWithPath("recipe_steps[].start_time").description("레시피 단계 시작 시간"),
                        fieldWithPath("recipe_steps[].end_time").description("레시피 단계 종료 시간"),
                        fieldWithPath("view_status.id").description("레시피 시청 상태 ID"),
                        fieldWithPath("view_status.viewed_at").description("레시피 마지막 시청 시간"),
                        fieldWithPath("view_status.last_play_seconds").description("레시피 마지막 재생 시간"),
                        fieldWithPath("view_status.created_at").description("레시피 시청 상태 생성 시간"),
                        fieldWithPath("ingredients_info.ingredients").description("레시피 재료 정보"),
                        fieldWithPath("ingredients_info.id").description("레시피 재료 정보 ID"),
                        fieldWithPath("ingredients_info.ingredients[].name").description(
                            "레시피 재료 이름"),
                        fieldWithPath("ingredients_info.ingredients[].amount").description(
                            "레시피 재료 양"),
                        fieldWithPath("ingredients_info.ingredients[].unit").description(
                            "레시피 재료 단위")
                    )
                ));

            verify(recipeService).findFullRecipe(recipeId, userId);

            var responseBody = response.extract().jsonPath();
            assertThat(responseBody.getString("recipe_status")).isEqualTo(
            fullRecipeInfo.getRecipeStatus().name());

            // 비디오 정보 검증
            assertThat(responseBody.getString("video_info.video_id")).isEqualTo(
                fullRecipeInfo.getVideoInfo().getVideoId());
            assertThat(responseBody.getString("video_info.video_title")).isEqualTo(
                fullRecipeInfo.getVideoInfo().getTitle());
            assertThat(responseBody.getString("video_info.video_thumbnail_url")).isEqualTo(
                fullRecipeInfo.getVideoInfo().getThumbnailUrl().toString());
            assertThat(responseBody.getInt("video_info.video_seconds")).isEqualTo(
                fullRecipeInfo.getVideoInfo().getVideoSeconds());

            // 재료 정보 검증
            assertThat(responseBody.getString("ingredients_info.id")).isEqualTo(
                fullRecipeInfo.getIngredientsInfo().getIngredientsId().toString());

            // 조회 상태 검증
            assertThat(responseBody.getString("view_status.id")).isEqualTo(
                fullRecipeInfo.getRecipeViewStatusInfo().getId().toString());
            assertThat(LocalDateTime.parse(responseBody.getString("view_status.viewed_at"))).isEqualTo(
                fullRecipeInfo.getRecipeViewStatusInfo().getViewedAt());
            assertThat(responseBody.getInt("view_status.last_play_seconds")).isEqualTo(
                fullRecipeInfo.getRecipeViewStatusInfo().getLastPlaySeconds());
            assertThat(LocalDateTime.parse(responseBody.getString("view_status.created_at"))).isEqualTo(
                fullRecipeInfo.getRecipeViewStatusInfo().getCreatedAt());

            for (int i = 0; i < fullRecipeInfo.getRecipeStepInfos().size(); i++) {
              var expectedStep = fullRecipeInfo.getRecipeStepInfos().get(i);
              var stepPath = "recipe_steps[" + i + "]";

              assertThat(responseBody.getUUID(stepPath + ".id")).isEqualTo(expectedStep.getId());
              assertThat(responseBody.getInt(stepPath + ".step_order")).isEqualTo(expectedStep.getStepOrder());
              assertThat(responseBody.getString(stepPath + ".subtitle")).isEqualTo(expectedStep.getSubtitle());
              assertThat(responseBody.getList(stepPath + ".details")).isEqualTo(expectedStep.getDetails());
              assertThat(responseBody.getDouble(stepPath + ".start_time")).isEqualTo(expectedStep.getStart());
              assertThat(responseBody.getDouble(stepPath + ".end_time")).isEqualTo(expectedStep.getEnd());
            }
          }
        }
      }

      @Nested
      @DisplayName("When - 추천 레시피를 조회할 때")
      class WhenRequestingRecommendRecipes {

        private List<RecipeOverview> recipeOverviews;

        @BeforeEach
        void setUp() {
          RecipeOverview recipeOverview = RecipeControllerTestData.recipeOverview;
          recipeOverviews = List.of(recipeOverview);
          when(recipeService.findRecommends()).thenReturn(recipeOverviews);
        }

        @Test
        @DisplayName("Then - 추천 레시피를 성공적으로 반환한다")
        void thenShouldReturnRecommendRecipes() {
          var response = given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .get("/api/v1/recipes/recommend")
              .then()
              .status(HttpStatus.OK)
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  responseFields(
                      fieldWithPath("recommend_recipes").description("추천 레시피 목록"),
                      fieldWithPath("recommend_recipes[].recipe_id").description("레시피 ID"),
                      fieldWithPath("recommend_recipes[].recipe_title").description("레시피 제목"),
                      fieldWithPath("recommend_recipes[].video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                      fieldWithPath("recommend_recipes[].video_id").description("레시피 비디오 ID"),
                      fieldWithPath("recommend_recipes[].count").description("레시피 조회 수")
                  )
              ));

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getList("recommend_recipes")).hasSize(1);

          verify(recipeService).findRecommends();
          recipeOverviews.forEach(recipeOverview -> {
            var actualPath = "recommend_recipes[" + recipeOverviews.indexOf(recipeOverview) + "]";

            assertThat(responseBody.getString(actualPath + ".recipe_id"))
                .isEqualTo(recipeOverview.getId().toString());
            assertThat(responseBody.getString(actualPath + ".recipe_title"))
                .isEqualTo(recipeOverview.getVideoInfo().getTitle());
            assertThat(responseBody.getString(actualPath + ".video_thumbnail_url"))
                .isEqualTo(recipeOverview.getVideoInfo().getThumbnailUrl().toString());
            assertThat(responseBody.getString(actualPath + ".video_id"))
                .isEqualTo(recipeOverview.getVideoInfo().getVideoId());
            assertThat(responseBody.getInt(actualPath + ".count"))
                .isEqualTo(recipeOverview.getCount());
          });
        }
      }
    }
}