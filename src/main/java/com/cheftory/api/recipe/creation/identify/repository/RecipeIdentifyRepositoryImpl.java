package com.cheftory.api.recipe.creation.identify.repository;

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
public class RecipeIdentifyRepositoryImpl implements RecipeIdentifyRepository {

    private final RecipeIdentifyJpaRepository repository;

    @Override
    public RecipeIdentify create(RecipeIdentify recipeIdentify, Clock clock) throws RecipeIdentifyException {
        try {
            return repository.save(recipeIdentify);
        } catch (DataIntegrityViolationException e) {
            throw new RecipeIdentifyException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING);
        }
    }

    @Override
    @Transactional
    public void delete(URI url) {
        repository.deleteByUrl(url);
    }
}
