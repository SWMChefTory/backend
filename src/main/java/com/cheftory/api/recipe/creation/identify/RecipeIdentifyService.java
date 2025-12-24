package com.cheftory.api.recipe.creation.identify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import jakarta.transaction.Transactional;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeIdentifyService {

  private final RecipeIdentifyRepository recipeIdentifyRepository;
  private final Clock clock;

  public RecipeIdentify create(URI url) {
    try {
      RecipeIdentify recipeIdentify = RecipeIdentify.create(url, clock);
      return recipeIdentifyRepository.save(recipeIdentify);
    } catch (DataIntegrityViolationException e) {
      throw new RecipeIdentifyException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING);
    }
  }

  @Transactional
  public void delete(URI url) {
    recipeIdentifyRepository.deleteByUrl(url);
  }
}
