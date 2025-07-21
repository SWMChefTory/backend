package com.cheftory.api.recipe.repository;


import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @Query("select r from Recipe r where r.videoInfo.videoUri = :videoUrl")
    List<Recipe> findAllByVideoUrl(URI videoUrl);

    @Modifying
    @Query("update Recipe r SET r.status= :status WHERE r.id =:id")
    int updateStatus(UUID id, @Param("status") RecipeStatus status);

    @Modifying
    @Query("update Recipe r set r.count = r.count + 1 where r.id = :id")
    int increaseCount(UUID id);

    @Modifying
    @Query("update Recipe r set r.captionCreatedAt = :captionCreatedAt where r.id = :id")
    int updateCaptionCreatedAt(UUID id, LocalDateTime captionCreatedAt);

    @Modifying
    @Query("update Recipe r set r.ingredientsCreatedAt = :ingredientsCreatedAt")
    int updateIngredientsCreatedAt(UUID id, LocalDateTime ingredientsCreatedAt);

    @Modifying
    @Query("update Recipe r set r.stepCreatedAt = :stepCreatedAt")
    int updateStepCreatedAt(UUID id,LocalDateTime stepCreatedAt);

    boolean existsByVideoInfo_VideoUri(URI videoUrl);

    @Query("select r from Recipe r where r.id in :recipeIds")
    List<Recipe> findRecipesById(List<UUID> recipeIds);
}
