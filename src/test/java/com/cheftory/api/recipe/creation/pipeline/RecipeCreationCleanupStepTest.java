package com.cheftory.api.recipe.creation.pipeline;

import static org.mockito.Mockito.*;

import com.cheftory.api.recipe.content.verify.RecipeVerifyService;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationCleanupStep")
class RecipeCreationCleanupStepTest {

    private RecipeVerifyService recipeVerifyService;
    private RecipeCreationCleanupStep sut;

    @BeforeEach
    void setUp() {
        recipeVerifyService = mock(RecipeVerifyService.class);
        sut = new RecipeCreationCleanupStep(recipeVerifyService);
    }

    @Test
    @DisplayName("context에 fileUri가 있으면 cleanup을 호출한다")
    void shouldCallCleanupWhenFileUriExists() {
        // Given
        UUID recipeId = UUID.randomUUID();
        String fileUri = "s3://bucket/file.mp4";
        RecipeCreationExecutionContext context = RecipeCreationExecutionContext.withFileInfo(
                RecipeCreationExecutionContext.of(recipeId, "video-123", URI.create("https://youtu.be/123")),
                fileUri, "video/mp4");

        // When
        sut.cleanup(context);

        // Then
        verify(recipeVerifyService).cleanup(fileUri);
    }

    @Test
    @DisplayName("context가 null이면 아무것도 하지 않는다")
    void shouldDoNothingWhenContextIsNull() {
        // When
        sut.cleanup(null);

        // Then
        verify(recipeVerifyService, never()).cleanup(anyString());
    }

    @Test
    @DisplayName("fileUri가 null이면 아무것도 하지 않는다")
    void shouldDoNothingWhenFileUriIsNull() {
        // Given
        RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(
                UUID.randomUUID(), "video-123", URI.create("https://youtu.be/123"));

        // When
        sut.cleanup(context);

        // Then
        verify(recipeVerifyService, never()).cleanup(anyString());
    }
}
