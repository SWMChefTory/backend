package com.cheftory.api.recipe.creation.identify;

import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIdentifyRepository extends JpaRepository<RecipeIdentify, UUID> {
  void deleteByUrl(URI uri);
}
