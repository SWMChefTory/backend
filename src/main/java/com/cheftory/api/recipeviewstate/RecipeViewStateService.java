package com.cheftory.api.recipeviewstate;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeviewstate.dto.SimpleAccessInfo;
import com.cheftory.api.recipeviewstate.entity.RecipeViewState;
import com.cheftory.api.recipeviewstate.exception.RecipeViewStateErrorCode;
import com.cheftory.api.recipeviewstate.exception.RecipeViewStateException;
import com.cheftory.api.recipeviewstate.repository.RecipeViewStateRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeViewStateService {
  private final RecipeViewStateRepository recipeViewStateRepository;
  private final Clock clock;

  public UUID create(UUID userId, UUID recipeViewStateId) {
    RecipeViewState recipeViewState = RecipeViewState.of(clock, userId, recipeViewStateId);
    return recipeViewStateRepository.save(recipeViewState).getRecipeId();
  }

  public SimpleAccessInfo findRecipeViewState(UUID recipeViewStateId) {
    RecipeViewState recipeViewState =  recipeViewStateRepository
        .findById(recipeViewStateId)
        .orElseThrow(()-> new RecipeViewStateException(
            RecipeViewStateErrorCode.RECIPE_VIEW_STATE_NOT_FOUND));

    return SimpleAccessInfo.from(recipeViewState);
  }

  public List<SimpleAccessInfo> find(UUID userId) {
    return recipeViewStateRepository.findByUserId(userId);
  }
}
