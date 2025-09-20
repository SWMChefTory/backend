package com.cheftory.api.recipeinfo.progress;

import com.cheftory.api._common.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeProgressService {
  private final RecipeProgressRepository recipeProgressRepository;
  private final Clock clock;

  public List<RecipeProgress> finds(UUID recipeId) {
    return recipeProgressRepository.findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
  }

  public void create(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
    RecipeProgress recipeProgress = RecipeProgress.create(recipeId, clock, step, detail);
    recipeProgressRepository.save(recipeProgress);
  }
}
