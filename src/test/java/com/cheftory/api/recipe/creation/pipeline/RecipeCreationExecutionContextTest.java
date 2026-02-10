package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("RecipeCreationExecutionContext 테스트")
class RecipeCreationExecutionContextTest {

    @Nested
    @DisplayName("생성 (of)")
    class Of {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            UUID recipeId;
            String videoId;
            URI videoUrl;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "test-video-id";
                videoUrl = URI.create("https://youtu.be/test");
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeCreationExecutionContext context;

                @BeforeEach
                void setUp() {
                    context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl, "test-title");
                }

                @Test
                @DisplayName("Then - 컨텍스트가 생성되고 기본 필드가 설정된다")
                void thenCreated() {
                    assertThat(context).isNotNull();
                    assertThat(context.getRecipeId()).isEqualTo(recipeId);
                    assertThat(context.getVideoId()).isEqualTo(videoId);
                    assertThat(context.getVideoUrl()).isEqualTo(videoUrl);
                    assertThat(context.getFileUri()).isNull();
                    assertThat(context.getMimeType()).isNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("파일 정보 추가 (withFileInfo)")
    class WithFileInfo {

        @Nested
        @DisplayName("Given - 기존 컨텍스트와 파일 정보가 주어졌을 때")
        class GivenContextAndFileInfo {
            RecipeCreationExecutionContext context;
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() {
                context = RecipeCreationExecutionContext.of(UUID.randomUUID(), "test-video-id", URI.create("https://youtu.be/test"), "test-title");
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
            }

            @Nested
            @DisplayName("When - 파일 정보 추가를 요청하면")
            class WhenAddingFileInfo {
                RecipeCreationExecutionContext newContext;

                @BeforeEach
                void setUp() {
                    newContext = RecipeCreationExecutionContext.withFileInfo(context, fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 파일 정보가 포함된 새 컨텍스트를 반환한다")
                void thenReturnsNewContext() {
                    assertThat(newContext).isNotNull();
                    assertThat(newContext.getRecipeId()).isEqualTo(context.getRecipeId());
                    assertThat(newContext.getVideoId()).isEqualTo(context.getVideoId());
                    assertThat(newContext.getVideoUrl()).isEqualTo(context.getVideoUrl());
                    assertThat(newContext.getFileUri()).isEqualTo(fileUri);
                    assertThat(newContext.getMimeType()).isEqualTo(mimeType);
                }
            }
        }
    }

    @Nested
    @DisplayName("조회 (getters)")
    class Getters {

        @Nested
        @DisplayName("Given - 파일 정보가 없는 컨텍스트일 때")
        class GivenNoFileInfo {
            RecipeCreationExecutionContext context;

            @BeforeEach
            void setUp() {
                context = RecipeCreationExecutionContext.of(UUID.randomUUID(), "test-video-id", URI.create("https://youtu.be/test"), "test-title");
            }

            @Test
            @DisplayName("Then - 파일 정보는 null을 반환한다")
            void thenReturnsNull() {
                assertThat(context.getFileUri()).isNull();
                assertThat(context.getMimeType()).isNull();
            }
        }

        @Nested
        @DisplayName("Given - 파일 정보가 있는 컨텍스트일 때")
        class GivenFileInfo {
            RecipeCreationExecutionContext context;
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() {
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(UUID.randomUUID(), "test-video-id", URI.create("https://youtu.be/test"), "test-title"),
                        fileUri,
                        mimeType);
            }

            @Test
            @DisplayName("Then - 설정된 파일 정보를 반환한다")
            void thenReturnsFileInfo() {
                assertThat(context.getFileUri()).isEqualTo(fileUri);
                assertThat(context.getMimeType()).isEqualTo(mimeType);
            }
        }
    }
}
