package com.cheftory.api.recipe.ingredients.repository;

import com.cheftory.api.recipe.ingredients.entity.RequiredIngredients;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeIngredientsRepository extends JpaRepository<RequiredIngredients,UUID> {
}
