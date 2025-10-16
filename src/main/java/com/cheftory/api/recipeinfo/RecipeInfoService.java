package com.cheftory.api.recipeinfo;

import com.cheftory.api.recipeinfo.briefing.RecipeBriefing;
import com.cheftory.api.recipeinfo.briefing.RecipeBriefingService;
import com.cheftory.api.recipeinfo.category.RecipeCategory;
import com.cheftory.api.recipeinfo.category.RecipeCategoryService;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipeinfo.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;
import com.cheftory.api.recipeinfo.identify.RecipeIdentifyService;
import com.cheftory.api.recipeinfo.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredient;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredientService;
import com.cheftory.api.recipeinfo.model.CountRecipeCategory;
import com.cheftory.api.recipeinfo.model.FullRecipeInfo;
import com.cheftory.api.recipeinfo.model.RecipeHistory;
import com.cheftory.api.recipeinfo.model.RecipeOverview;
import com.cheftory.api.recipeinfo.model.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.RecipeProgressService;
import com.cheftory.api.recipeinfo.recipe.RecipeService;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipeinfo.search.RecipeSearch;
import com.cheftory.api.recipeinfo.search.RecipeSearchService;
import com.cheftory.api.recipeinfo.step.RecipeStepService;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.tag.RecipeTagService;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatusCount;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatusService;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaErrorCode;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeInfoService {

  private final AsyncRecipeInfoCreationService asyncRecipeInfoCreationService;
  private final RecipeStepService recipeStepService;
  private final RecipeViewStatusService recipeViewStatusService;
  private final RecipeCategoryService recipeCategoryService;
  private final RecipeYoutubeMetaService recipeYoutubeMetaService;
  private final RecipeIngredientService recipeIngredientService;
  private final RecipeDetailMetaService recipeDetailMetaService;
  private final RecipeProgressService recipeProgressService;
  private final RecipeTagService recipeTagService;
  private final RecipeIdentifyService recipeIdentifyService;
  private final RecipeBriefingService recipeBriefingService;
  private final RecipeService recipeService;
  private final RecipeSearchService recipeSearchService;

  /**
   * 기존 레시피가 있으면 그 레시피를, 없으면 새로 생성합니다.
   *
   * @param uri 유튜브 영상 URL
   * @param userId 사용자 ID
   * @return 레시피 ID
   */
  public UUID create(URI uri, UUID userId) {
    try {
      List<UUID> recipeIds =
          recipeYoutubeMetaService.getByUrl(uri).stream()
              .map(RecipeYoutubeMeta::getRecipeId)
              .toList();
      Recipe recipe = recipeService.getNotFailed(recipeIds);
      recipeViewStatusService.create(userId, recipe.getId());
      return recipe.getId();
    } catch (RecipeInfoException e) {
      if (e.getErrorMessage() == RecipeErrorCode.RECIPE_NOT_FOUND) {
        return createNewRecipe(uri, userId);
      }
      if (e.getErrorMessage() == RecipeErrorCode.RECIPE_FAILED) {
        return createNewRecipe(uri, userId);
      }
      if (e.getErrorMessage() == YoutubeMetaErrorCode.YOUTUBE_META_BANNED) {
        throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_BANNED);
      }
      throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }
  }

  /** 새로운 유튜브 메타 데이터를 생성하고, 새로운 레시피를 생성합니다. 만약 유니크 키 중복 오류가 발생하면(동시성 문제) 기존 레시피를 사용합니다. */
  public UUID createNewRecipe(URI uri, UUID userId) {
    try {
      YoutubeVideoInfo videoInfo = recipeYoutubeMetaService.getVideoInfo(uri);
      recipeIdentifyService.create(uri);
      UUID recipeId = recipeService.create();
      recipeYoutubeMetaService.create(videoInfo, recipeId);
      asyncRecipeInfoCreationService.create(recipeId, videoInfo.getVideoId(), uri);
      recipeViewStatusService.create(userId, recipeId);
      return recipeId;
    } catch (RecipeInfoException e) {
      if (e.getErrorMessage() == RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING) {
        List<UUID> recipeIds =
            recipeYoutubeMetaService.getByUrl(uri).stream()
                .map(RecipeYoutubeMeta::getRecipeId)
                .toList();
        Recipe recipe = recipeService.getNotFailed(recipeIds);
        recipeViewStatusService.create(userId, recipe.getId());
        return recipe.getId();
      }
      throw e;
    }
  }

  /**
   * 레시피 상세 정보를 조회합니다. * 레시피가 존재하지 않으면 예외를 던집니다. * 레시피가 실패 상태이면 예외를 던집니다. 레시피가 성공 상태이면 조회수를 증가시킵니다.
   */
  public FullRecipeInfo getFullRecipe(UUID recipeId, UUID userId) {
    try {
      Recipe recipe = recipeService.getSuccess(recipeId);
      List<RecipeStep> steps = recipeStepService.gets(recipeId);
      List<RecipeIngredient> ingredients = recipeIngredientService.gets(recipeId);
      RecipeDetailMeta detailMeta = recipeDetailMetaService.get(recipeId);
      List<RecipeProgress> progresses = recipeProgressService.gets(recipeId);
      List<RecipeTag> tags = recipeTagService.gets(recipeId);
      List<RecipeBriefing> briefings = recipeBriefingService.gets(recipeId);
      RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaService.get(recipeId);
      RecipeViewStatus viewStatus = recipeViewStatusService.get(userId, recipeId);

      return FullRecipeInfo.of(
          steps,
          ingredients,
          detailMeta,
          progresses,
          tags,
          youtubeMeta,
          viewStatus,
          recipe,
          briefings);
    } catch (RecipeInfoException e) {
      if (e.getErrorMessage() == RecipeErrorCode.RECIPE_NOT_FOUND
          || e.getErrorMessage() == RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND) {
        throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);
      }
      if (e.getErrorMessage() == RecipeErrorCode.RECIPE_FAILED) {
        throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_FAILED);
      }
      throw e;
    }
  }

  /**
   * 추천 레시피 목록을 조회합니다. 성공한 레시피만 가져옵니다. 유튜브 메타데이터가 없는 레시피는 제외합니다. (정상적인 상황은 아님)
   *
   * @param page 페이지 번호 (0부터 시작)
   * @return 레시피 개요 페이지
   */
  public Page<RecipeOverview> getPopulars(Integer page) {
    Page<Recipe> recipes = recipeService.getPopulars(page);

    List<UUID> recipeIds = recipes.stream().map(Recipe::getId).toList();

    Map<UUID, RecipeYoutubeMeta> youtubeMetaMap =
        recipeYoutubeMetaService.getByRecipes(recipeIds).stream()
            .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity()));

    Map<UUID, RecipeDetailMeta> detailMetaMap =
        recipeDetailMetaService.getIn(recipeIds).stream()
            .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

    Map<UUID, List<RecipeTag>> tagsMap =
        recipeTagService.getIn(recipeIds).stream()
            .collect(Collectors.groupingBy(RecipeTag::getRecipeId));

    List<RecipeOverview> recipeOverviews =
        recipes.getContent().stream()
            .map(
                recipe -> {
                  UUID recipeId = recipe.getId();
                  RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                  if (youtubeMeta == null) {
                    log.error("완료된 레시피의 유튜브 메타데이터 누락: recipeId={}", recipeId);
                    return null;
                  }

                  RecipeDetailMeta detailMeta = detailMetaMap.get(recipe.getId());
                  if (detailMeta == null) {
                    log.warn("레시피 상세 메타데이터 누락: recipeId={}", recipe.getId());
                  }

                  List<RecipeTag> tags =
                      tagsMap.getOrDefault(recipe.getId(), Collections.emptyList());

                  if (tags.isEmpty()) {
                    log.error("레시피의 태그 누락: recipeId={}", recipe.getId());
                  }

                  return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags);
                })
            .filter(Objects::nonNull)
            .toList();

    return new PageImpl<>(recipeOverviews, recipes.getPageable(), recipes.getTotalElements());
  }

  public Page<RecipeHistory> getRecents(UUID userId, Integer page) {
    Page<RecipeViewStatus> viewStatuses = recipeViewStatusService.getRecentUsers(userId, page);
    return buildHistoryOverviews(viewStatuses);
  }

  public Page<RecipeHistory> getCategorized(UUID userId, UUID recipeCategoryId, Integer page) {
    Page<RecipeViewStatus> viewStatuses =
        recipeViewStatusService.getCategories(userId, recipeCategoryId, page);
    return buildHistoryOverviews(viewStatuses);
  }

  public Page<RecipeHistory> getUnCategorized(UUID userId, Integer page) {
    Page<RecipeViewStatus> viewStatuses = recipeViewStatusService.getUnCategories(userId, page);
    return buildHistoryOverviews(viewStatuses);
  }

  /**
   * 히스토리 목록을 만듭니다. 1) viewStatus에서 recipeId 수집 2) recipeId로 실패하지 않은 Recipe 불러오기 (in_progress,
   * success) 3) recipeId로 RecipeYoutube 불러오기 (metaId 수집) 3-2) metaId 목록 수집 4) metaId로
   * RecipeYoutubeMeta 불러오기 5) 매핑: 누락은 스킵하며 로그
   *
   * @param viewStatuses 뷰 상태 페이지
   * @return 히스토리 페이지
   */
  private Page<RecipeHistory> buildHistoryOverviews(Page<RecipeViewStatus> viewStatuses) {
    List<UUID> recipeIds = viewStatuses.stream().map(RecipeViewStatus::getRecipeId).toList();

    Map<UUID, Recipe> recipeMap =
        recipeService.getsNotFailed(recipeIds).stream()
            .collect(Collectors.toMap(Recipe::getId, Function.identity()));

    Map<UUID, RecipeYoutubeMeta> youtubeMetaMap =
        recipeYoutubeMetaService.getByRecipes(recipeIds).stream()
            .collect(
                Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity(), (a, b) -> a));

    Map<UUID, RecipeDetailMeta> detailMetaMap =
        recipeDetailMetaService.getIn(recipeIds).stream()
            .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

    List<RecipeHistory> histories =
        viewStatuses.getContent().stream()
            .map(
                viewStatus -> {
                  UUID recipeId = viewStatus.getRecipeId();

                  Recipe recipe = recipeMap.get(recipeId);
                  if (recipe == null) {
                    log.warn(
                        "히스토리: 존재하지 않는 레시피 recipeId={}, userId={}",
                        recipeId,
                        viewStatus.getUserId());
                    return null;
                  }

                  RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                  if (youtubeMeta == null) {
                    log.warn("히스토리: 유튜브 메타 엔티티 누락 recipeId={}", recipeId);
                    return null;
                  }

                  RecipeDetailMeta detailMeta = detailMetaMap.get(recipeId);

                  return RecipeHistory.of(recipe, viewStatus, youtubeMeta, detailMeta);
                })
            .filter(Objects::nonNull)
            .toList();

    return new PageImpl<>(histories, viewStatuses.getPageable(), viewStatuses.getTotalElements());
  }

  public List<CountRecipeCategory> getCategories(UUID userId) {
    var categories = recipeCategoryService.getUsers(userId);
    var categoryIds = categories.stream().map(RecipeCategory::getId).toList();
    var counts = recipeViewStatusService.countByCategories(categoryIds);

    var countMap =
        counts.stream()
            .collect(
                Collectors.toMap(
                    RecipeViewStatusCount::getCategoryId, RecipeViewStatusCount::getCount));

    return categories.stream()
        .map(
            category ->
                CountRecipeCategory.of(category, countMap.getOrDefault(category.getId(), 0)))
        .toList();
  }

  public void deleteCategory(UUID categoryId) {
    recipeViewStatusService.deleteCategories(categoryId);
    recipeCategoryService.delete(categoryId);
  }

  public RecipeProgressStatus getRecipeProgress(UUID recipeId) {
    List<RecipeProgress> progresses = recipeProgressService.gets(recipeId);
    Recipe recipe = recipeService.get(recipeId);
    return RecipeProgressStatus.of(recipe, progresses);
  }

  public Page<RecipeOverview> searchRecipes(Integer page, String query) {

    Page<RecipeSearch> searchResults = recipeSearchService.search(query, page);

    List<UUID> recipeIds =
        searchResults.getContent().stream().map(RecipeSearch::getId).map(UUID::fromString).toList();

    Map<UUID, Recipe> recipeMap =
        recipeService.gets(recipeIds).stream()
            .collect(Collectors.toMap(Recipe::getId, Function.identity()));

    Map<UUID, RecipeYoutubeMeta> youtubeMetaMap =
        recipeYoutubeMetaService.getByRecipes(recipeIds).stream()
            .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity()));

    Map<UUID, RecipeDetailMeta> detailMetaMap =
        recipeDetailMetaService.getIn(recipeIds).stream()
            .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

    Map<UUID, List<RecipeTag>> tagsMap =
        recipeTagService.getIn(recipeIds).stream()
            .collect(Collectors.groupingBy(RecipeTag::getRecipeId));

    List<RecipeOverview> recipeOverviews =
        searchResults
            .map(
                recipeSearch -> {
                  UUID recipeId = UUID.fromString(recipeSearch.getId());
                  Recipe recipe = recipeMap.get(recipeId);
                  if (recipe == null) {
                    log.error("검색된 레시피가 존재하지 않음: recipeId={}", recipeId);
                    return null;
                  }
                  RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                  if (youtubeMeta == null) {
                    log.error("완료된 레시피의 유튜브 메타데이터 누락: recipeId={}", recipeId);
                    return null;
                  }

                  RecipeDetailMeta detailMeta = detailMetaMap.get(recipe.getId());
                  if (detailMeta == null) {
                    log.warn("레시피 상세 메타데이터 누락: recipeId={}", recipe.getId());
                  }

                  List<RecipeTag> tags =
                      tagsMap.getOrDefault(recipe.getId(), Collections.emptyList());

                  if (tags.isEmpty()) {
                    log.error("레시피의 태그 누락: recipeId={}", recipe.getId());
                  }

                  return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags);
                })
            .filter(Objects::nonNull)
            .toList();

    return new PageImpl<>(
        recipeOverviews, searchResults.getPageable(), searchResults.getTotalElements());
  }
}
