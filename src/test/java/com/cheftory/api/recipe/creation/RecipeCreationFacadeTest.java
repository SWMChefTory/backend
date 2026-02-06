package com.cheftory.api.recipe.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
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
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeCreationFacade 테스트")
class RecipeCreationFacadeTest {

	private AsyncRecipeCreationService asyncRecipeCreationService;
	private RecipeBookmarkService recipeBookmarkService;
	private RecipeYoutubeMetaService recipeYoutubeMetaService;
	private RecipeProgressService recipeProgressService;
	private RecipeIdentifyService recipeIdentifyService;
	private RecipeInfoService recipeInfoService;
	private RecipeCreditPort creditPort;
	private RecipeCreationTxService recipeCreationTxService;

	private RecipeCreationFacade sut;

	@BeforeEach
	void setUp() {
		asyncRecipeCreationService = mock(AsyncRecipeCreationService.class);
		recipeBookmarkService = mock(RecipeBookmarkService.class);
		recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
		recipeProgressService = mock(RecipeProgressService.class);
		recipeIdentifyService = mock(RecipeIdentifyService.class);
		recipeInfoService = mock(RecipeInfoService.class);
		creditPort = mock(RecipeCreditPort.class);
		recipeCreationTxService = mock(RecipeCreationTxService.class);

		sut = new RecipeCreationFacade(
				asyncRecipeCreationService,
				recipeBookmarkService,
				recipeYoutubeMetaService,
				recipeProgressService,
				recipeIdentifyService,
				recipeInfoService,
				creditPort,
				recipeCreationTxService);
	}

	@Nested
	@DisplayName("기존 레시피 사용")
	class UseExistingRecipe {

		@Test
		@DisplayName("기존 레시피가 있고 User 요청이며 북마크가 생성되면 credit을 차감한다")
		void shouldUseExistingRecipeAndSpendCredit() {
			URI uri = URI.create("https://youtube.com/watch?v=test");
			UUID userId = UUID.randomUUID();
			UUID recipeId = UUID.randomUUID();
			long creditCost = 100L;

			RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
			RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

			doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
			doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);
			doReturn(true).when(recipeBookmarkService).create(userId, recipeId);

			UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

			assertThat(result).isEqualTo(recipeId);
			verify(recipeBookmarkService).create(userId, recipeId);
			verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
			verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
		}

		@Test
		@DisplayName("기존 레시피가 있고 User 요청이지만 북마크 생성이 실패하면 credit을 차감하지 않는다")
		void shouldNotSpendWhenBookmarkNotCreated() {
			URI uri = URI.create("https://youtube.com/watch?v=test");
			UUID userId = UUID.randomUUID();
			UUID recipeId = UUID.randomUUID();
			long creditCost = 100L;

			RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
			RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

			doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
			doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);
			doReturn(false).when(recipeBookmarkService).create(userId, recipeId);

			UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

			assertThat(result).isEqualTo(recipeId);
			verify(recipeBookmarkService).create(userId, recipeId);
			verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());
			verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
		}

		@Test
		@DisplayName("기존 레시피가 있고 Crawler 요청이면 북마크/credit 없이 기존 레시피를 반환한다")
		void shouldUseExistingRecipeWithoutBookmarkForCrawler() {
			URI uri = URI.create("https://youtube.com/watch?v=test");
			UUID recipeId = UUID.randomUUID();
			long creditCost = 100L;

			RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
			RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

			doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
			doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);

			UUID result = sut.createBookmark(new RecipeCreationTarget.Crawler(uri));

			assertThat(result).isEqualTo(recipeId);
			verify(recipeBookmarkService, never()).create(any(), any());
			verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());
			verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
		}

		@Test
		@DisplayName("유튜브 메타가 BANNED이면 RECIPE_BANNED 예외를 던진다")
		void shouldThrowWhenYoutubeMetaBanned() {
			URI uri = URI.create("https://youtube.com/watch?v=banned");
			UUID userId = UUID.randomUUID();

			doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED))
					.when(recipeYoutubeMetaService)
					.getByUrl(uri);

			assertThatThrownBy(() -> sut.createBookmark(new RecipeCreationTarget.User(uri, userId)))
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

			assertThatThrownBy(() -> sut.createBookmark(new RecipeCreationTarget.User(uri, userId)))
					.isInstanceOf(RecipeException.class)
					.hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_CREATE_FAIL);
		}
	}

	@Nested
	@DisplayName("새 레시피 생성")
	class CreateNewRecipe {

		@Test
		@DisplayName("유튜브 메타가 없으면 새 레시피를 생성하고 async 생성 프로세스를 시작한다")
		void shouldCreateNewRecipeAndStartAsync() {
			URI uri = URI.create("https://youtube.com/watch?v=new");
			UUID userId = UUID.randomUUID();

			YoutubeVideoInfo videoInfo = mockVideoInfo("test_video_id");
			UUID recipeId = UUID.randomUUID();
			long creditCost = 77L;
			RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

			doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
					.when(recipeYoutubeMetaService)
					.getByUrl(uri);

			doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
			doReturn(recipeInfo).when(recipeCreationTxService).createWithIdentifyWithVideoInfo(videoInfo);
			doReturn(true).when(recipeBookmarkService).create(userId, recipeId);

			UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

			assertThat(result).isEqualTo(recipeId);

			verify(recipeCreationTxService).createWithIdentifyWithVideoInfo(videoInfo);
			verify(recipeBookmarkService).create(userId, recipeId);
			verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);

			verify(asyncRecipeCreationService).create(recipeId, creditCost, videoInfo.getVideoId(), uri);

			verify(recipeYoutubeMetaService, never()).create(any(), any());
		}

		@Test
		@DisplayName("새 레시피 생성 중 identify progressing이면 기존 레시피를 조회해 사용한다")
		void shouldUseExistingRecipeWhenIdentifyProgressing() {
			URI uri = URI.create("https://youtube.com/watch?v=concurrent");
			UUID userId = UUID.randomUUID();

			YoutubeVideoInfo videoInfo = mockVideoInfo("test_video_id");
			UUID recipeId = UUID.randomUUID();
			long creditCost = 50L;

			RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
			RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);

			doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
					.when(recipeYoutubeMetaService)
					.getByUrl(uri);

			doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);

			doThrow(new RecipeException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING))
					.when(recipeCreationTxService)
					.createWithIdentifyWithVideoInfo(videoInfo);

			doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
			doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);
			doReturn(true).when(recipeBookmarkService).create(userId, recipeId);

			UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

			assertThat(result).isEqualTo(recipeId);
			verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any());
			verify(recipeBookmarkService).create(userId, recipeId);
			verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
		}

		@Test
		@DisplayName("기존 레시피 조회 시 RECIPE_FAILED면 새 레시피 생성으로 넘어간다")
		void shouldCreateNewRecipeWhenRecipeFailed() {
			URI uri = URI.create("https://youtube.com/watch?v=failed");
			UUID userId = UUID.randomUUID();

			UUID recipeId = UUID.randomUUID();
			long creditCost = 77L;

			RecipeYoutubeMeta meta = mockYoutubeMeta(recipeId);
			RecipeInfo recipeInfo = mockRecipeInfo(recipeId, creditCost);
			YoutubeVideoInfo videoInfo = mockVideoInfo("new_video_id");

			doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
			doThrow(new RecipeException(RecipeInfoErrorCode.RECIPE_FAILED))
					.when(recipeInfoService)
					.getSuccess(recipeId);

			doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
			doReturn(recipeInfo).when(recipeCreationTxService).createWithIdentifyWithVideoInfo(videoInfo);
			doReturn(true).when(recipeBookmarkService).create(userId, recipeId);

			UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

			assertThat(result).isEqualTo(recipeId);
			verify(asyncRecipeCreationService).create(recipeId, creditCost, videoInfo.getVideoId(), uri);
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
		URI uri = UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + videoId)
				.build()
				.toUri();
		YoutubeUri youtubeUri = YoutubeUri.from(uri);
		return YoutubeVideoInfo.from(
				youtubeUri,
				"테스트 요리 영상",
				"테스트 채널",
				URI.create("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg"),
				300,
				YoutubeMetaType.NORMAL);
	}
}
