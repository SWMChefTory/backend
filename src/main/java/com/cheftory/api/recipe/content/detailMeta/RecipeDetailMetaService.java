package com.cheftory.api.recipe.content.detailMeta;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailMetaService {
  private final RecipeDetailMetaRepository recipeDetailMetaRepository;
  private final Clock clock;

  public RecipeDetailMeta get(UUID recipeId) {
    return recipeDetailMetaRepository
        .findByRecipeId(recipeId)
        .orElseThrow(
            () -> new RecipeDetailMetaException(RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND));
  }

  public List<RecipeDetailMeta> getIn(List<UUID> recipeIds) {
    return recipeDetailMetaRepository.findAllByRecipeIdIn(recipeIds);
  }

  public void create(UUID recipeId, Integer cookTime, Integer servings, String description) {
    RecipeDetailMeta recipeDetailMeta =
        RecipeDetailMeta.create(cookTime, servings, description, clock, recipeId);
    recipeDetailMetaRepository.save(recipeDetailMeta);
  }
}
