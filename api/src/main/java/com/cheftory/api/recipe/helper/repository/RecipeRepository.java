package com.cheftory.api.recipe.helper.repository;


import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @Query("select r from Recipe r where r.videoInfo.videoUri = :videoUrl")
    Optional<Recipe> findByVideoUri(URI videoUrl);

    @Modifying
    @Query("update Recipe r SET r.status= :status WHERE r.id =:id")
    int updateStatus(UUID id, @Param("status") RecipeStatus status);

    @Modifying
    @Query("update Recipe r set r.count = r.count + 1 where r.id = :id")
    int increaseCount(UUID id);
}
