package com.cheftory.api.recipe.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.history.RecipeHistoryService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeCreationFacade 테스트")
class RecipeCreationFacadeTest {

  private AsyncRecipeCreationService asyncRecipeCreationService;
  private RecipeHistoryService recipeHistoryService;
  private RecipeYoutubeMetaService recipeYoutubeMetaService;
  private RecipeIdentifyService recipeIdentifyService;
  private RecipeInfoService recipeInfoService;
  private RecipeCreditPort creditPort;

  private RecipeCreationFacade sut;

  @BeforeEach
  void setUp() {
    asyncRecipeCreationService = mock(AsyncRecipeCreationService.class);
    recipeHistoryService = mock(RecipeHistoryService.class);
    recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
    recipeIdentifyService = mock(RecipeIdentifyService.class);
    recipeInfoService = mock(RecipeInfoService.class);
    creditPort = mock(RecipeCreditPort.class);

    sut =
        new RecipeCreationFacade(
            asyncRecipeCreationService,
            recipeHistoryService,
            recipeYoutubeMetaService,
            recipeIdentifyService,
            recipeInfoService,
            creditPort);
  }

  @Nested
  @DisplayName("기존 레시피 사용")
  class UseExistingRecipe {

    @Test
    @DisplayName("기존 레시피가 있고 User 요청이며 히스토리가 생성되면 credit을 차감한다")
    void shouldUseExistingRecipeAndSpendCredit() {
      URI uri = URI.create("https://youtube.com/watch?v=test");
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();
      long creditCost = 100L;

      RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
      RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipeInfo).when(recipeInfoService).getNotFailed(List.of(recipeId));
      doReturn(true).when(recipeHistoryService).create(userId, recipeId);

      UUID result = sut.create(new RecipeCreationTarget.User(uri, userId));

      assertThat(result).isEqualTo(recipeId);
      verify(recipeHistoryService).create(userId, recipeId);
      verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);

      verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("기존 레시피가 있고 User 요청이지만 히스토리 생성이 실패하면 credit을 차감하지 않는다")
    void shouldNotSpendWhenHistoryNotCreated() {
      URI uri = URI.create("https://youtube.com/watch?v=test");
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();
      long creditCost = 100L;

      RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
      RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipeInfo).when(recipeInfoService).getNotFailed(List.of(recipeId));
      doReturn(false).when(recipeHistoryService).create(userId, recipeId);

      UUID result = sut.create(new RecipeCreationTarget.User(uri, userId));

      assertThat(result).isEqualTo(recipeId);
      verify(recipeHistoryService).create(userId, recipeId);
      verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());

      verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("기존 레시피가 있고 Crawler 요청이면 히스토리/credit 없이 기존 레시피를 반환한다")
    void shouldUseExistingRecipeWithoutHistoryForCrawler() {
      URI uri = URI.create("https://youtube.com/watch?v=test");
      UUID recipeId = UUID.randomUUID();
      long creditCost = 100L;

      RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
      RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipeInfo).when(recipeInfoService).getNotFailed(List.of(recipeId));

      UUID result = sut.create(new RecipeCreationTarget.Crawler(uri));

      assertThat(result).isEqualTo(recipeId);
      verify(recipeHistoryService, never()).create(any(), any());
      verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());
      verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
    }

    @Test
    @DisplayName("밴된 영상이면 RECIPE_BANNED 예외를 던진다")
    void shouldThrowWhenBanned() {
      URI uri = URI.create("https://youtube.com/watch?v=banned");
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();

      RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED))
          .when(recipeInfoService)
          .getNotFailed(anyList());

      assertThatThrownBy(() -> sut.create(new RecipeCreationTarget.User(uri, userId)))
          .isInstanceOf(RecipeException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_BANNED);
    }

    @Test
    @DisplayName("알 수 없는 RecipeException이면 RECIPE_CREATE_FAIL 예외를 던진다")
    void shouldThrowCreateFailForUnknownRecipeException() {
      URI uri = URI.create("https://youtube.com/watch?v=blocked");
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED))
          .when(recipeYoutubeMetaService)
          .getByUrl(uri);

      assertThatThrownBy(() -> sut.create(new RecipeCreationTarget.User(uri, userId)))
          .isInstanceOf(RecipeException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_CREATE_FAIL);
    }
  }

  @Nested
  @DisplayName("새 레시피 생성")
  class CreateNewRecipe {

    @Test
    @DisplayName("기존 레시피가 없으면 새 레시피를 생성하고 async 생성 프로세스를 시작한다")
    void shouldCreateNewRecipeAndStartAsync() {
      URI uri = URI.create("https://youtube.com/watch?v=new");
      UUID userId = UUID.randomUUID();

      YoutubeVideoInfo videoInfo = mockVideoInfo("test_video_id");
      UUID recipeId = UUID.randomUUID();
      long creditCost = 77L;
      RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

      doReturn(List.of()).when(recipeYoutubeMetaService).getByUrl(uri);
      doThrow(new RecipeException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
          .when(recipeInfoService)
          .getNotFailed(anyList());

      doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
      doReturn(recipeInfo).when(recipeInfoService).create();
      doReturn(true).when(recipeHistoryService).create(userId, recipeId);

      UUID result = sut.create(new RecipeCreationTarget.User(uri, userId));

      assertThat(result).isEqualTo(recipeId);
      verify(recipeIdentifyService).create(uri);
      verify(recipeYoutubeMetaService).create(videoInfo, recipeId);
      verify(recipeHistoryService).create(userId, recipeId);
      verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);

      verify(asyncRecipeCreationService).create(recipeId, creditCost, videoInfo.getVideoId(), uri);
    }

    @Test
    @DisplayName("동시성으로 identify가 progressing이면 기존 레시피를 조회해 사용한다")
    void shouldUseExistingRecipeWhenIdentifyProgressing() {
      URI uri = URI.create("https://youtube.com/watch?v=concurrent");
      UUID userId = UUID.randomUUID();

      YoutubeVideoInfo videoInfo = mockVideoInfo("test_video_id");
      UUID recipeId = UUID.randomUUID();
      long creditCost = 50L;

      RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
      RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

      doReturn(List.of()).when(recipeYoutubeMetaService).getByUrl(uri);
      doThrow(new RecipeException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
          .when(recipeInfoService)
          .getNotFailed(anyList());

      doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
      doThrow(new RecipeException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING))
          .when(recipeIdentifyService)
          .create(uri);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipeInfo).when(recipeInfoService).getNotFailed(List.of(recipeId));
      doReturn(true).when(recipeHistoryService).create(userId, recipeId);

      UUID result = sut.create(new RecipeCreationTarget.User(uri, userId));

      assertThat(result).isEqualTo(recipeId);
      verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
      verify(recipeHistoryService).create(userId, recipeId);
      verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
    }
  }

  private RecipeYoutubeMeta mockYoutubeMeta(UUID recipeId) {
    RecipeYoutubeMeta meta = mock(RecipeYoutubeMeta.class);
    doReturn(recipeId).when(meta).getRecipeId();
    return meta;
  }

  private RecipeInfo mockRecipeInfo(UUID recipeId, long creditCost) {
    RecipeInfo recipeInfo = mock(RecipeInfo.class);
    doReturn(recipeId).when(recipeInfo).getId();
    doReturn(creditCost).when(recipeInfo).getCreditCost();
    return recipeInfo;
  }

  private YoutubeVideoInfo mockVideoInfo(String videoId) {
    URI uri =
        UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + videoId)
            .build()
            .toUri();
    YoutubeUri youtubeUri = YoutubeUri.from(uri);
    return YoutubeVideoInfo.from(
        youtubeUri,
        "테스트 요리 영상",
        URI.create("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg"),
        300,
        YoutubeMetaType.NORMAL);
  }
}
