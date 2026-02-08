package com.cheftory.api.recipe.category;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.repository.RecipeCategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCategoryService {

    private final RecipeCategoryRepository recipeCategoryRepository;
    private final Clock clock;

    public UUID create(String name, UUID userId) {
        RecipeCategory recipeCategory = RecipeCategory.create(clock, name, userId);
        return recipeCategoryRepository.create(recipeCategory);
    }

    public void delete(UUID userId, UUID recipeCategoryId) {
        recipeCategoryRepository.delete(userId, recipeCategoryId);
    }

    public List<RecipeCategory> getUsers(UUID userId) {
        return recipeCategoryRepository.gets(userId);
    }

    public boolean exists(UUID recipeCategoryId) {
        return recipeCategoryRepository.exists(recipeCategoryId);
    }
}
