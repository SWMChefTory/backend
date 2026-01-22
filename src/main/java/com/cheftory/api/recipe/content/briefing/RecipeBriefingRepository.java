package com.cheftory.api.recipe.content.briefing;

import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeBriefingRepository extends JpaRepository<RecipeBriefing, UUID> {
    List<RecipeBriefing> findAllByRecipeId(UUID recipeId);
}
