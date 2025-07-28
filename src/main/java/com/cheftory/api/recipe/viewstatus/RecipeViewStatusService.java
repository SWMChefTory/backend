package com.cheftory.api.recipe.viewstatus;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.viewstatus.exception.ViewStatusErrorCode;
import com.cheftory.api.recipe.viewstatus.exception.ViewStatusException;
import com.cheftory.api.recipe.viewstatus.repository.ViewStatusRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeViewStatusService {
  private final ViewStatusRepository viewStatusRepository;
  private final Clock clock;

  @Transactional
  public void create(UUID userId, UUID recipeId) {
    viewStatusRepository.findByRecipeIdAndUserId(recipeId, userId)
        .orElseGet(() -> viewStatusRepository
            .save(RecipeViewStatus.of(clock, userId, recipeId))
        );
  }

  @Transactional
  public RecipeViewStatus find(UUID userId, UUID recipeId) {
    RecipeViewStatus recipeViewStatus = viewStatusRepository.findByRecipeIdAndUserId(recipeId, userId)
        .orElseThrow(() -> new ViewStatusException(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND));
    recipeViewStatus.updateViewedAt(clock);
    return viewStatusRepository.save(recipeViewStatus);
  }

  public List<RecipeViewStatus> findRecentUsers(UUID userId) {
    return viewStatusRepository.findByUserId(userId, ViewStatusSort.VIEWED_AT_DESC);
  }
}
