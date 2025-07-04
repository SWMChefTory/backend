package com.cheftory.api.recipe.step.repository;

import com.cheftory.api.recipe.step.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, UUID> {
}
