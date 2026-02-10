package com.cheftory.api.recipe.content.briefing.respotiory;

import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 브리핑 JPA Repository
 */
public interface RecipeBriefingJpaRepository extends JpaRepository<RecipeBriefing, UUID> {
    List<RecipeBriefing> findAllByRecipeId(UUID recipeId);
}
