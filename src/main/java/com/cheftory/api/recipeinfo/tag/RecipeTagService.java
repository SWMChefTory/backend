package com.cheftory.api.recipeinfo.tag;

import com.cheftory.api._common.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeTagService {
  private final RecipeTagRepository recipeTagRepository;
  private final Clock clock;

  public List<RecipeTag> finds(UUID recipeId) {
    return recipeTagRepository.findAllByRecipeId(recipeId);
  }

  public List<RecipeTag> findIn(List<UUID> recipeIds) {
    return recipeTagRepository.findAllByRecipeIdIn(recipeIds);
  }

  public void create(UUID recipeId, List<String> tags) {
    List<RecipeTag> recipeTags =
        tags.stream().map(tag -> RecipeTag.create(tag, recipeId, clock)).toList();
    recipeTagRepository.saveAll(recipeTags);
  }
}
