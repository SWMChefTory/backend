package com.cheftory.api.recipe.content.scene.client.dto;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.scene.entity.RecipeScene;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneErrorCode;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 외부 scene 생성 API 응답 DTO.
 */
public record ClientRecipeScenesResponse(
        @JsonProperty("scenes") List<Scene> scenes) {

    public List<RecipeScene> toRecipeScenes(UUID recipeId, Clock clock) throws RecipeSceneException {
        List<RecipeScene> recipeScenes = new ArrayList<>();
        List<Scene> rawScenes = scenes == null ? List.of() : scenes;
        for (Scene scene : rawScenes) {
            recipeScenes.add(scene.toRecipeScene(recipeId, clock));
        }
        return recipeScenes;
    }

    public record Scene(
            @JsonProperty("step_id") UUID stepId,
            @JsonProperty("label") String label,
            @JsonProperty("start") Double start,
            @JsonProperty("end") Double end,
            @JsonProperty("important_score") Integer importantScore) {

        private RecipeScene toRecipeScene(UUID recipeId, Clock clock) throws RecipeSceneException {
            if (stepId == null || label == null || start == null || end == null || importantScore == null) {
                throw new RecipeSceneException(RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL);
            }
            if (start < 0 || end < 0 || end < start) {
                throw new RecipeSceneException(RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL);
            }
            return RecipeScene.create(stepId, recipeId, label, start, end, importantScore, clock);
        }
    }
}
