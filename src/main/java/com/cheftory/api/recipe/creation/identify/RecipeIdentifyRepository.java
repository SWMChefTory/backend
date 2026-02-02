package com.cheftory.api.recipe.creation.identify;

import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIdentifyRepository extends JpaRepository<RecipeIdentify, UUID> {
    Optional<RecipeIdentify> findByUrl(URI uri);

    void deleteByUrl(URI uri);
}
