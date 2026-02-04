package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("RecipeCreationExecutionContext 테스트")
class RecipeCreationExecutionContextTest {

    @Nested
    @DisplayName("of 정적 팩토리 메서드")
    class OfMethod {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            private UUID recipeId;
            private String videoId;
            private URI videoUrl;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "test-video-id";
                videoUrl = URI.create("https://youtu.be/test");
            }

            @Test
            @DisplayName("When - of를 호출하면 Then - 컨텍스트가 생성되고 모든 필드가 설정된다")
            void thenCreatesContextWithAllFieldsSet() {
                RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

                assertThat(context).isNotNull();
                assertThat(context.getRecipeId()).isEqualTo(recipeId);
                assertThat(context.getVideoId()).isEqualTo(videoId);
                assertThat(context.getVideoUrl()).isEqualTo(videoUrl);
                assertThat(context.getFileUri()).isNull();
                assertThat(context.getMimeType()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("withFileInfo 정적 팩토리 메서드")
    class WithFileInfoMethod {

        @Nested
        @DisplayName("Given - 기존 컨텍스트와 file 정보가 주어졌을 때")
        class GivenExistingContextAndFileInfo {

            private UUID recipeId;
            private String videoId;
            private URI videoUrl;
            private RecipeCreationExecutionContext context;
            private String fileUri;
            private String mimeType;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "test-video-id";
                videoUrl = URI.create("https://youtu.be/test");
                context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
            }

            @Test
            @DisplayName("When - withFileInfo를 호출하면 Then - 새 컨텍스트가 생성되고 file 정보가 설정된다")
            void thenCreatesNewContextWithFileInfo() {
                RecipeCreationExecutionContext newContext =
                        RecipeCreationExecutionContext.withFileInfo(context, fileUri, mimeType);

                assertThat(newContext).isNotNull();
                assertThat(newContext.getRecipeId()).isEqualTo(recipeId);
                assertThat(newContext.getVideoId()).isEqualTo(videoId);
                assertThat(newContext.getVideoUrl()).isEqualTo(videoUrl);
                assertThat(newContext.getFileUri()).isEqualTo(fileUri);
                assertThat(newContext.getMimeType()).isEqualTo(mimeType);
            }
        }
    }

    @Nested
    @DisplayName("getter 메서드")
    class Getters {

        @Nested
        @DisplayName("Given - 컨텍스트가 있을 때")
        class GivenExistingContext {

            private UUID recipeId;
            private String videoId;
            private URI videoUrl;
            private String fileUri;
            private String mimeType;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "test-video-id";
                videoUrl = URI.create("https://youtu.be/test");
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
            }

            @Test
            @DisplayName("Then - file 정보가 null이면 fileUri/mimeType은 null을 반환한다")
            void thenGetFileInfoReturnsNullWhenEmpty() {
                RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

                assertThat(context.getFileUri()).isNull();
                assertThat(context.getMimeType()).isNull();
            }

            @Test
            @DisplayName("Then - file 정보가 설정되면 getFileUri/getMimeType을 반환한다")
            void thenGetFileInfoReturnsWhenSet() {
                RecipeCreationExecutionContext context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl), fileUri, mimeType);

                assertThat(context.getFileUri()).isEqualTo(fileUri);
                assertThat(context.getMimeType()).isEqualTo(mimeType);
            }
        }
    }
}
