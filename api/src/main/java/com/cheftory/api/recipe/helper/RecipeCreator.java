package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.helper.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeCreator {
    private final RecipeRepository recipeRepository;

    public UUID create(VideoInfo videoInfo) {
        log.trace("recipe create.");
        Recipe recipe = recipeRepository
                .save(Recipe.preCompletedOf(videoInfo));
        log.trace("recipe id : " + recipe.getId());
        return recipe.getId();
    }
}
