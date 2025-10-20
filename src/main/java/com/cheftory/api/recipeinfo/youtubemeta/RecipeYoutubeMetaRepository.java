package com.cheftory.api.recipeinfo.youtubemeta;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeYoutubeMetaRepository extends JpaRepository<RecipeYoutubeMeta, UUID> {
  List<RecipeYoutubeMeta> findAllByVideoUri(URI videoUri);

  List<RecipeYoutubeMeta> findAllByRecipeIdIn(List<UUID> recipeIds);

  Optional<RecipeYoutubeMeta> findByRecipeId(UUID recipeId);

  Page<RecipeYoutubeMeta> findByStatus(YoutubeMetaStatus status, Pageable pageable);
}
