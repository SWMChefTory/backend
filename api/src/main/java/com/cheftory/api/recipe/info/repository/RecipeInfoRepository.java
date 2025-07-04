package com.cheftory.api.recipe.info.repository;

import com.cheftory.api.recipe.info.entity.RecipeInfo;
import com.cheftory.api.recipe.info.entity.RecipeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RecipeInfoRepository extends JpaRepository<RecipeInfo, UUID> {

    @Modifying
    @Query("update RecipeInfo r SET r.status= :status WHERE r.id =:id")
    int updateStatus(UUID id, @Param("status")RecipeStatus status);
}
