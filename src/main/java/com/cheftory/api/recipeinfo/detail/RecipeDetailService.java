package com.cheftory.api.recipeinfo.detail;

import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.detail.client.RecipeDetailClient;
import com.cheftory.api.recipeinfo.detail.entity.RecipeDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailService {

  private final RecipeDetailClient recipeDetailClient;

  public RecipeDetail getRecipeDetails(String videoId, RecipeCaption recipeCaption) {
    return recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption).toRecipeDetail();
  }
}
