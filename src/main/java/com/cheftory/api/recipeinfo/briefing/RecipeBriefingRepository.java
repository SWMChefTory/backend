package com.cheftory.api.recipeinfo.briefing;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeBriefingRepository extends JpaRepository<RecipeBriefing, UUID> {
  List<RecipeBriefing> findAllByRecipeId(UUID recipeId);
}
