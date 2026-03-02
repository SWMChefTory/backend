package com.cheftory.api.recipe.content.youtubemeta.repository;

import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link RecipeYoutubeMeta} JPA 저장소.
 */
public interface RecipeYoutubeMetaJpaRepository extends JpaRepository<RecipeYoutubeMeta, UUID> {
    /**
     * 동일 비디오를 참조하는 레시피 메타 목록을 조회합니다.
     */
    List<RecipeYoutubeMeta> findAllByVideoId(String videoId);

    /**
     * 레시피 ID 목록에 대응하는 메타를 일괄 조회합니다.
     */
    List<RecipeYoutubeMeta> findAllByRecipeIdIn(List<UUID> recipeIds);

    /**
     * 레시피 ID로 단건 메타를 조회합니다.
     */
    Optional<RecipeYoutubeMeta> findByRecipeId(UUID recipeId);

    /**
     * 레시피 ID 기준 메타 존재 여부를 확인합니다.
     */
    boolean existsByRecipeId(UUID recipeId);
}
