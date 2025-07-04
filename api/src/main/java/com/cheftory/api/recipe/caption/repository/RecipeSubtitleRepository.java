package com.cheftory.api.recipe.caption.repository;

import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeSubtitleRepository extends JpaRepository<RecipeCaption, UUID> {
}
