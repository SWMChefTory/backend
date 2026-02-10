package com.cheftory.api.recipe.content.youtubemeta.repository;

import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeYoutubeMetaJpaRepository extends JpaRepository<RecipeYoutubeMeta, UUID> {
    List<RecipeYoutubeMeta> findAllByVideoUri(URI videoUri);

    List<RecipeYoutubeMeta> findAllByRecipeIdIn(List<UUID> recipeIds);

    Optional<RecipeYoutubeMeta> findByRecipeId(UUID recipeId);
}
