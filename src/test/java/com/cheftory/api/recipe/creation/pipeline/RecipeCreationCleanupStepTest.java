package com.cheftory.api.recipe.creation.pipeline;

import static org.mockito.Mockito.*;

import com.cheftory.api.recipe.content.verify.RecipeVerifyService;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationCleanupStep 테스트")
class RecipeCreationCleanupStepTest {

    private RecipeVerifyService recipeVerifyService;
    private RecipeCreationCleanupStep sut;

    @BeforeEach
    void setUp() {
        recipeVerifyService = mock(RecipeVerifyService.class);
        sut = new RecipeCreationCleanupStep(recipeVerifyService);
    }

    @Nested
    @DisplayName("정리 (cleanup)")
    class Cleanup {

        @Nested
        @DisplayName("Given - 파일 URI가 있는 컨텍스트일 때")
        class GivenFileUri {
            RecipeCreationExecutionContext context;
            String fileUri;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/file.mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(
                                UUID.randomUUID(), "video-123", URI.create("https://youtu.be/123"), "test-title"),
                        fileUri,
                        "video/mp4");
            }

            @Nested
            @DisplayName("When - 정리를 요청하면")
            class WhenCleaningUp {

                @BeforeEach
                void setUp() {
                    sut.cleanup(context);
                }

                @Test
                @DisplayName("Then - 파일을 정리한다")
                void thenCleansUpFile() {
                    verify(recipeVerifyService).cleanup(fileUri);
                }
            }
        }

        @Nested
        @DisplayName("Given - 컨텍스트가 null일 때")
        class GivenNullContext {

            @Nested
            @DisplayName("When - 정리를 요청하면")
            class WhenCleaningUp {

                @BeforeEach
                void setUp() {
                    sut.cleanup(null);
                }

                @Test
                @DisplayName("Then - 아무것도 하지 않는다")
                void thenDoesNothing() {
                    verify(recipeVerifyService, never()).cleanup(anyString());
                }
            }
        }

        @Nested
        @DisplayName("Given - 파일 URI가 없는 컨텍스트일 때")
        class GivenNoFileUri {
            RecipeCreationExecutionContext context;

            @BeforeEach
            void setUp() {
                context = RecipeCreationExecutionContext.of(
                        UUID.randomUUID(), "video-123", URI.create("https://youtu.be/123"), null);
            }

            @Nested
            @DisplayName("When - 정리를 요청하면")
            class WhenCleaningUp {

                @BeforeEach
                void setUp() {
                    sut.cleanup(context);
                }

                @Test
                @DisplayName("Then - 아무것도 하지 않는다")
                void thenDoesNothing() {
                    verify(recipeVerifyService, never()).cleanup(anyString());
                }
            }
        }
    }
}
