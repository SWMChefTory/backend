package com.cheftory.api.recipeinfo.step;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.step.client.RecipeStepClient;
import com.cheftory.api.recipeinfo.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.step.entity.RecipeStepSort;
import com.cheftory.api.recipeinfo.step.repository.RecipeStepRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeStepService {
  private final RecipeStepClient recipeStepClient;
  private final RecipeStepRepository recipeStepRepository;
  private final Clock clock;

  public List<UUID> create(UUID recipeId, RecipeCaption recipeCaption) {
    ClientRecipeStepsResponse response = recipeStepClient.fetchRecipeSteps(recipeCaption);

    List<RecipeStep> recipeSteps = response.toRecipeSteps(recipeId, clock);

    return recipeStepRepository.saveAll(recipeSteps).stream().map(RecipeStep::getId).toList();
  }

  public List<RecipeStep> gets(UUID recipeId) {
    return recipeStepRepository.findAllByRecipeId(recipeId, RecipeStepSort.STEP_ORDER_ASC);
  }
}
