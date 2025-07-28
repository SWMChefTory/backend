package com.cheftory.api.recipe;


import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @Query("select r from Recipe r where r.videoInfo.videoUri = :videoUrl")
    List<Recipe> findAllByVideoUrl(URI videoUrl);

    @Modifying
    @Transactional
    @Query("update Recipe r SET r.status= :status WHERE r.id =:id")
    void updateStatus(UUID id, @Param("status") RecipeStatus status);

    @Modifying
    @Transactional
    @Query("update Recipe r set r.count = r.count + 1 where r.id = :id")
    void increaseCount(UUID id);


    @Query("select r from Recipe r where r.id in :recipeIds")
    List<Recipe> findRecipesById(List<UUID> recipeIds);

    List<Recipe> findByStatus(RecipeStatus status, Sort sort);

}
