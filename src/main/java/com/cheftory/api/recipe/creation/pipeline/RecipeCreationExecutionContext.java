package com.cheftory.api.recipe.creation.pipeline;

import java.net.URI;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeCreationExecutionContext {
    private UUID recipeId;
    private String videoId;
    private URI videoUrl;
    private @Nullable String fileUri;
    private @Nullable String mimeType;
    private String title;

    public static RecipeCreationExecutionContext of(UUID recipeId, String videoId, URI videoUrl, String title) {
        return new RecipeCreationExecutionContext(recipeId, videoId, videoUrl, null, null, title);
    }

    public static RecipeCreationExecutionContext withFileInfo(
            RecipeCreationExecutionContext context, String fileUri, String mimeType) {
        return new RecipeCreationExecutionContext(
                context.recipeId, context.videoId, context.videoUrl, fileUri, mimeType, context.title);
    }
}
