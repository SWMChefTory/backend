package com.cheftory.api.recipe.content.info;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeSort;
import com.cheftory.api.recipe.util.RecipePageRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeInfoService {
  private final RecipeInfoRepository recipeInfoRepository;
  private final Clock clock;
  private final I18nTranslator i18nTranslator;

  public RecipeInfo getSuccess(UUID recipeId) {

    RecipeInfo recipeInfo =
        recipeInfoRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));

    if (recipeInfo.isFailed()) {
      throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_FAILED);
    }

    recipeInfoRepository.increaseCount(recipeId);
    return recipeInfo;
  }

  public RecipeInfo getNotFailed(List<UUID> recipeIds) {
    List<RecipeInfo> recipeInfos = recipeInfoRepository.findAllByIdIn(recipeIds);

    if (recipeInfos.isEmpty()) {
      throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);
    }

    if (recipeInfos.stream().allMatch(RecipeInfo::isFailed)) {
      throw new RecipeInfoException(RecipeInfoErrorCode.RECIPE_FAILED);
    }

    List<RecipeInfo> validRecipeInfos = recipeInfos.stream().filter(r -> !r.isFailed()).toList();

    if (validRecipeInfos.size() > 1) {
      log.warn(
          "여러 개의 progress 및 success 상태 Recipe가 조회되었습니다. recipeIds={}, count={}",
          recipeIds,
          validRecipeInfos.size());
    }

    return validRecipeInfos.getFirst();
  }

  public RecipeInfo create() {
    RecipeInfo recipeInfo = RecipeInfo.create(clock);
    recipeInfoRepository.save(recipeInfo);
    return recipeInfo;
  }

  public List<RecipeInfo> getValidRecipes(List<UUID> recipeIds) {
    return recipeInfoRepository
        .findRecipesByIdInAndRecipeStatusIn(
            recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS))
        .stream()
        .toList();
  }

  public List<RecipeInfo> gets(List<UUID> recipeIds) {
    return recipeInfoRepository.findAllByIdIn(recipeIds);
  }

  public Page<RecipeInfo> getPopulars(Integer page, RecipeInfoVideoQuery videoQuery) {
    Pageable pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

    return switch (videoQuery) {
      case ALL -> recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);
      case NORMAL, SHORTS ->
          recipeInfoRepository.findRecipes(RecipeStatus.SUCCESS, pageable, videoQuery.name());
    };
  }

  public RecipeInfo success(UUID recipeId) {
    RecipeInfo recipeInfo =
        recipeInfoRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
    recipeInfo.success(clock);
    return recipeInfoRepository.save(recipeInfo);
  }

  public RecipeInfo failed(UUID recipeId) {
    RecipeInfo recipeInfo =
        recipeInfoRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
    recipeInfo.failed(clock);
    return recipeInfoRepository.save(recipeInfo);
  }

  public void block(UUID recipeId) {
    RecipeInfo recipeInfo =
        recipeInfoRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
    recipeInfo.block(clock);
    recipeInfoRepository.save(recipeInfo);
  }

  public boolean exists(UUID recipeId) {
    return recipeInfoRepository.existsById(recipeId);
  }

  public RecipeInfo get(UUID recipeId) {
    return recipeInfoRepository
        .findById(recipeId)
        .orElseThrow(() -> new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND));
  }

  public Page<RecipeInfo> getCuisines(RecipeCuisineType type, Integer page) {
    Pageable pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);
    String cuisine = i18nTranslator.translate(type.messageKey());
    return recipeInfoRepository.findCuisineRecipes(cuisine, RecipeStatus.SUCCESS, pageable);
  }
}
