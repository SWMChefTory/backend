package com.cheftory.api.recipeviewstate.repository;

import com.cheftory.api.recipeviewstate.dto.ViewStateInfo;
import com.cheftory.api.recipeviewstate.entity.RecipeViewState;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeViewStateRepository extends JpaRepository<RecipeViewState, UUID> {

  List<ViewStateInfo> findByUserId(UUID userId);
}
