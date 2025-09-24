package com.cheftory.api.recipeinfo.identify;

import java.net.URI;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIdentifyRepository extends JpaRepository<RecipeIdentify, UUID> {
  void deleteByUrl(URI uri);
}
