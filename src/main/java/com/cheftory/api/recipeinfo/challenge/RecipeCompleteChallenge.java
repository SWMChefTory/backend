package com.cheftory.api.recipeinfo.challenge;

import com.cheftory.api._common.PocOnly;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@PocOnly(until = "2025-12-31")
public class RecipeCompleteChallenge {

  private UUID recipeId;
  private boolean isFinished;

  public static RecipeCompleteChallenge of(UUID recipeId, boolean isFinished) {
    return RecipeCompleteChallenge.builder().recipeId(recipeId).isFinished(isFinished).build();
  }
}
