package com.cheftory.api.recipe.search;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    mockMvc =
        mockMvcBuilder(controller)
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

    @Nested
    @DisplayName("Given - 유효한 검색어가 주어졌을 때")
    class GivenValidSearchQuery {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "김치찌개";
        page = 0;
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        private Page<RecipeOverview> searchResults;
        private RecipeOverview recipeOverview;
        private RecipeInfo recipeInfo;
        private RecipeYoutubeMeta youtubeMeta;
        private RecipeDetailMeta detailMeta;
        private UUID recipeId;
        private Pageable pageable;
        private List<String> tags;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recipeInfo = mock(RecipeInfo.class);
          recipeOverview = mock(RecipeOverview.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          detailMeta = mock(RecipeDetailMeta.class);
          pageable = Pageable.ofSize(10);

          doReturn(recipeId).when(recipeInfo).getId();

          doReturn("김치찌개 맛있게 끓이는 법").when(youtubeMeta).getTitle();
          doReturn("kimchi_video_id").when(youtubeMeta).getVideoId();
          doReturn(URI.create("https://example.com/kimchi_thumbnail.jpg"))
              .when(youtubeMeta)
              .getThumbnailUrl();
          doReturn(300).when(youtubeMeta).getVideoSeconds();

          doReturn("얼큰한 김치찌개 레시피").when(detailMeta).getDescription();
          doReturn(2).when(detailMeta).getServings();
          doReturn(30).when(detailMeta).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId).when(recipeOverview).getRecipeId();
          doReturn("김치찌개 맛있게 끓이는 법").when(recipeOverview).getVideoTitle();
          doReturn(URI.create("https://example.com/kimchi_thumbnail.jpg"))
              .when(recipeOverview)
              .getThumbnailUrl();
          doReturn("kimchi_video_id").when(recipeOverview).getVideoId();
          doReturn(URI.create("https://youtube.com/watch?v=kimchi_video_id"))
              .when(recipeOverview)
              .getVideoUri();
          doReturn(300).when(recipeOverview).getVideoSeconds();
          doReturn(500).when(recipeOverview).getViewCount();
          doReturn(YoutubeMetaType.NORMAL).when(recipeOverview).getVideoType();
          doReturn("얼큰한 김치찌개 레시피").when(recipeOverview).getDescription();
          doReturn(2).when(recipeOverview).getServings();
          doReturn(30).when(recipeOverview).getCookTime();
          doReturn(7L).when(recipeOverview).getCreditCost();
          doReturn(List.of("한식", "찌개")).when(recipeOverview).getTags();
          doReturn(true).when(recipeOverview).getIsViewed();

          System.out.println("Mock setup - recipeId: " + recipeOverview.getRecipeId());
          System.out.println("Mock setup - videoTitle: " + recipeOverview.getVideoTitle());
          System.out.println("Mock setup - tags: " + recipeOverview.getTags());

          searchResults = new PageImpl<>(List.of(recipeOverview), pageable, 1);
          doReturn(searchResults)
              .when(recipeSearchFacade)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 검색 결과를 성공적으로 반환해야 한다")
        void thenShouldReturnSearchResults() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("query", query)
                  .param("page", page)
                  .get("/api/v1/recipes/search")
                  .then()
                  .status(HttpStatus.OK)
                  .extract()
                  .response();

          response.then().body("searched_recipes", hasSize(1));

          response
              .then()
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
                      requestPreprocessor(),
                      responsePreprocessor(),
                      queryParameters(
                          parameterWithName("query").description("검색어"),
                          parameterWithName("page")
                              .description("페이지 번호 (0부터 시작, 기본값: 0)")
                              .optional()),
                      responseFields(
                          fieldWithPath("searched_recipes").description("검색된 레시피 목록"),
                          fieldWithPath("searched_recipes[].recipe_id").description("레시피 ID"),
                          fieldWithPath("searched_recipes[].recipe_title").description("레시피 제목"),
                          fieldWithPath("searched_recipes[].tags").description("레시피 태그 목록"),
                          fieldWithPath("searched_recipes[].tags[].name").description("태그 이름"),
                          fieldWithPath("searched_recipes[].description").description("레시피 설명"),
                          fieldWithPath("searched_recipes[].servings").description("인분"),
                          fieldWithPath("searched_recipes[].cooking_time").description("조리 시간(분)"),
                          fieldWithPath("searched_recipes[].video_id").description("레시피 비디오 ID"),
                          fieldWithPath("searched_recipes[].count").description("레시피 조회 수"),
                          fieldWithPath("searched_recipes[].video_url").description("레시피 비디오 URL"),
                          fieldWithPath("searched_recipes[].video_type")
                              .description("비디오 타입 (NORMAL 또는 SHORTS)"),
                          fieldWithPath("searched_recipes[].video_thumbnail_url")
                              .description("레시피 비디오 썸네일 URL"),
                          fieldWithPath("searched_recipes[].video_seconds")
                              .description("레시피 비디오 재생 시간"),
                          fieldWithPath("searched_recipes[].is_viewed").description("레시피 조회 여부"),
                          fieldWithPath("current_page").description("현재 페이지 번호"),
                          fieldWithPath("total_pages").description("전체 페이지 수"),
                          fieldWithPath("total_elements").description("전체 요소 수"),
                          fieldWithPath("has_next").description("다음 페이지 존재 여부"),
                          fieldWithPath("searched_recipes[].credit_cost")
                              .description("레시피 크레딧 비용"))));

          verify(recipeSearchFacade).searchRecipes(page, query, userId);

          var responseBody = response.jsonPath();
          assertThat(responseBody.getList("searched_recipes")).hasSize(1);
          assertThat(responseBody.getString("searched_recipes[0].recipe_id"))
              .isEqualTo(recipeId.toString());
          assertThat(responseBody.getString("searched_recipes[0].recipe_title"))
              .isEqualTo("김치찌개 맛있게 끓이는 법");
          assertThat(responseBody.getList("searched_recipes[0].tags")).hasSize(2);
          assertThat(responseBody.getString("searched_recipes[0].tags[0].name")).isEqualTo("한식");
          assertThat(responseBody.getString("searched_recipes[0].tags[1].name")).isEqualTo("찌개");
          assertThat(responseBody.getString("searched_recipes[0].description"))
              .isEqualTo("얼큰한 김치찌개 레시피");
          assertThat(responseBody.getInt("searched_recipes[0].servings")).isEqualTo(2);
          assertThat(responseBody.getInt("searched_recipes[0].cooking_time")).isEqualTo(30);
          assertThat(responseBody.getString("searched_recipes[0].video_id"))
              .isEqualTo("kimchi_video_id");
          assertThat(responseBody.getInt("searched_recipes[0].count")).isEqualTo(500);
          assertThat(responseBody.getString("searched_recipes[0].video_url"))
              .isEqualTo("https://youtube.com/watch?v=kimchi_video_id");
          assertThat(responseBody.getString("searched_recipes[0].video_type")).isEqualTo("NORMAL");
          assertThat(responseBody.getString("searched_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/kimchi_thumbnail.jpg");
          assertThat(responseBody.getInt("searched_recipes[0].video_seconds")).isEqualTo(300);
          assertThat(responseBody.getBoolean("searched_recipes[0].is_viewed")).isEqualTo(true);
          assertThat(responseBody.getLong("searched_recipes[0].credit_cost")).isEqualTo(7L);
          assertThat(responseBody.getString("current_page")).isEqualTo("0");
          assertThat(responseBody.getString("total_pages")).isEqualTo("1");
          assertThat(responseBody.getString("total_elements")).isEqualTo("1");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
        }
      }
    }

    @Nested
    @DisplayName("Given - 검색 결과가 없는 검색어가 주어졌을 때")
    class GivenQueryWithNoResults {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "존재하지않는레시피";
        page = 0;
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
          pageable = Pageable.ofSize(10);
          doReturn(new PageImpl<RecipeOverview>(List.of(), pageable, 0))
              .when(recipeSearchFacade)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyResults() {
          given()
              .contentType(ContentType.JSON)
              .param("query", query)
              .param("page", page)
              .get("/api/v1/recipes/search")
              .then()
              .status(HttpStatus.OK)
              .body("searched_recipes", hasSize(0));

          verify(recipeSearchFacade).searchRecipes(page, query, userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 여러 검색 결과가 있는 검색어가 주어졌을 때")
    class GivenQueryWithMultipleResults {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "찌개";
        page = 0;
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        private Page<RecipeOverview> searchResults;
        private List<RecipeOverview> recipeOverviews;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          pageable = Pageable.ofSize(10);

          // 첫 번째 레시피 - 김치찌개
          RecipeOverview kimchiStew = mock(RecipeOverview.class);
          RecipeInfo recipeInfo1 = mock(RecipeInfo.class);
          RecipeYoutubeMeta meta1 = mock(RecipeYoutubeMeta.class);
          RecipeDetailMeta detail1 = mock(RecipeDetailMeta.class);
          UUID recipeId1 = UUID.randomUUID();

          doReturn(recipeId1).when(recipeInfo1).getId();
          doReturn("김치찌개").when(meta1).getTitle();
          doReturn("kimchi_video_id").when(meta1).getVideoId();
          doReturn(URI.create("https://example.com/kimchi.jpg")).when(meta1).getThumbnailUrl();
          doReturn(300).when(meta1).getVideoSeconds();
          doReturn("김치찌개 레시피").when(detail1).getDescription();
          doReturn(2).when(detail1).getServings();
          doReturn(30).when(detail1).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId1).when(kimchiStew).getRecipeId();
          doReturn("김치찌개").when(kimchiStew).getVideoTitle();
          doReturn(URI.create("https://example.com/kimchi.jpg")).when(kimchiStew).getThumbnailUrl();
          doReturn(URI.create("https://youtube.com/watch?v=kimchi_video_id"))
              .when(kimchiStew)
              .getVideoUri();
          doReturn("kimchi_video_id").when(kimchiStew).getVideoId();
          doReturn(300).when(kimchiStew).getVideoSeconds();
          doReturn(100).when(kimchiStew).getViewCount();
          doReturn(YoutubeMetaType.NORMAL).when(kimchiStew).getVideoType();
          doReturn("김치찌개 레시피").when(kimchiStew).getDescription();
          doReturn(2).when(kimchiStew).getServings();
          doReturn(30).when(kimchiStew).getCookTime();
          doReturn(List.of("한식")).when(kimchiStew).getTags();
          doReturn(true).when(kimchiStew).getIsViewed();

          // 두 번째 레시피 - 된장찌개
          RecipeOverview soyBeanStew = mock(RecipeOverview.class);
          RecipeInfo recipeInfo2 = mock(RecipeInfo.class);
          RecipeYoutubeMeta meta2 = mock(RecipeYoutubeMeta.class);
          RecipeDetailMeta detail2 = mock(RecipeDetailMeta.class);
          UUID recipeId2 = UUID.randomUUID();

          doReturn(recipeId2).when(recipeInfo2).getId();
          doReturn("된장찌개").when(meta2).getTitle();
          doReturn("soybean_video_id").when(meta2).getVideoId();
          doReturn(URI.create("https://example.com/soybean.jpg")).when(meta2).getThumbnailUrl();
          doReturn(240).when(meta2).getVideoSeconds();
          doReturn("된장찌개 레시피").when(detail2).getDescription();
          doReturn(2).when(detail2).getServings();
          doReturn(25).when(detail2).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId2).when(soyBeanStew).getRecipeId();
          doReturn("된장찌개").when(soyBeanStew).getVideoTitle();
          doReturn(URI.create("https://example.com/soybean.jpg"))
              .when(soyBeanStew)
              .getThumbnailUrl();
          doReturn(URI.create("https://youtube.com/watch?v=soybean_video_id"))
              .when(soyBeanStew)
              .getVideoUri();
          doReturn("soybean_video_id").when(soyBeanStew).getVideoId();
          doReturn(240).when(soyBeanStew).getVideoSeconds();
          doReturn(200).when(soyBeanStew).getViewCount();
          doReturn(YoutubeMetaType.NORMAL).when(soyBeanStew).getVideoType();
          doReturn("된장찌개 레시피").when(soyBeanStew).getDescription();
          doReturn(2).when(soyBeanStew).getServings();
          doReturn(25).when(soyBeanStew).getCookTime();
          doReturn(List.of("한식")).when(soyBeanStew).getTags();
          doReturn(true).when(soyBeanStew).getIsViewed();

          recipeOverviews = List.of(kimchiStew, soyBeanStew);
          searchResults = new PageImpl<>(recipeOverviews, pageable, 2);
          doReturn(searchResults)
              .when(recipeSearchFacade)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 모든 검색 결과를 성공적으로 반환해야 한다")
        void thenShouldReturnAllSearchResults() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("query", query)
                  .param("page", page)
                  .get("/api/v1/recipes/search")
                  .then()
                  .status(HttpStatus.OK)
                  .body("searched_recipes", hasSize(2));

          verify(recipeSearchFacade).searchRecipes(page, query, userId);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getList("searched_recipes")).hasSize(2);
          assertThat(responseBody.getString("searched_recipes[0].recipe_title")).isEqualTo("김치찌개");
          assertThat(responseBody.getString("searched_recipes[1].recipe_title")).isEqualTo("된장찌개");
          assertThat(responseBody.getString("total_elements")).isEqualTo("2");
        }
      }
    }

    @Nested
    @DisplayName("Given - 페이지네이션이 필요한 검색 결과가 주어졌을 때")
    class GivenPaginatedSearchResults {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "파스타";
        page = 1; // 두 번째 페이지
      }

      @Nested
      @DisplayName("When - 두 번째 페이지를 검색한다면")
      class WhenSearchingSecondPage {

        private Page<RecipeOverview> searchResults;

        @BeforeEach
        void setUp() {
          RecipeOverview recipe = mock(RecipeOverview.class);
          RecipeInfo recipeInfoEntity = mock(RecipeInfo.class);
          RecipeYoutubeMeta meta = mock(RecipeYoutubeMeta.class);
          RecipeDetailMeta detailMeta = mock(RecipeDetailMeta.class);
          UUID recipeId = UUID.randomUUID();

          doReturn(recipeId).when(recipeInfoEntity).getId();
          doReturn("까르보나라 파스타").when(meta).getTitle();
          doReturn("pasta_video_id").when(meta).getVideoId();
          doReturn(URI.create("https://example.com/pasta.jpg")).when(meta).getThumbnailUrl();
          doReturn(180).when(meta).getVideoSeconds();
          doReturn("파스타 레시피").when(detailMeta).getDescription();
          doReturn(2).when(detailMeta).getServings();
          doReturn(20).when(detailMeta).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId).when(recipe).getRecipeId();
          doReturn("까르보나라 파스타").when(recipe).getVideoTitle();
          doReturn(URI.create("https://example.com/pasta.jpg")).when(recipe).getThumbnailUrl();
          doReturn(URI.create("https://youtube.com/watch?v=pasta_video_id"))
              .when(recipe)
              .getVideoUri();
          doReturn("pasta_video_id").when(recipe).getVideoId();
          doReturn(180).when(recipe).getVideoSeconds();
          doReturn(150).when(recipe).getViewCount();
          doReturn(YoutubeMetaType.NORMAL).when(recipe).getVideoType();
          doReturn("파스타 레시피").when(recipe).getDescription();
          doReturn(2).when(recipe).getServings();
          doReturn(20).when(recipe).getCookTime();
          doReturn(List.of("양식")).when(recipe).getTags();
          doReturn(true).when(recipe).getIsViewed();

          // 전체 12개 결과 중 두 번째 페이지 (10개 이후의 2개)
          searchResults = new PageImpl<>(List.of(recipe), Pageable.ofSize(10).withPage(1), 11);
          doReturn(searchResults)
              .when(recipeSearchFacade)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 두 번째 페이지 결과를 성공적으로 반환해야 한다")
        void thenShouldReturnSecondPageResults() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("query", query)
                  .param("page", page)
                  .get("/api/v1/recipes/search")
                  .then()
                  .status(HttpStatus.OK);

          verify(recipeSearchFacade).searchRecipes(page, query, userId);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getString("current_page")).isEqualTo("1");
          assertThat(responseBody.getString("total_pages")).isEqualTo("2");
          assertThat(responseBody.getString("total_elements")).isEqualTo("11");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
        }
      }
    }
  }
}
