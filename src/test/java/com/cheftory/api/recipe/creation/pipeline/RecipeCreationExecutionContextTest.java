package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.*;
import javax.annotation.Nullable;

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
                assertThat(context.getCaption()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("from 정적 팩토리 메서드")
    class FromMethod {

        @Nested
        @DisplayName("Given - 기존 컨텍스트와 캡션이 주어졌을 때")
        class GivenExistingContextAndCaption {

            private UUID recipeId;
            private String videoId;
            private URI videoUrl;
            private RecipeCaption caption;
            private RecipeCreationExecutionContext context;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "test-video-id";
                videoUrl = URI.create("https://youtu.be/test");
                caption = mock(RecipeCaption.class);
                context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
            }

            @Test
            @DisplayName("When - from를 호출하면 Then - 새 컨텍스트가 생성되고 캡션이 설정된다")
            void thenCreatesNewContextWithCaptionSet() {
                RecipeCreationExecutionContext newContext = RecipeCreationExecutionContext.from(context, caption);

                assertThat(newContext).isNotNull();
                assertThat(newContext.getRecipeId()).isEqualTo(recipeId);
                assertThat(newContext.getVideoId()).isEqualTo(videoId);
                assertThat(newContext.getVideoUrl()).isEqualTo(videoUrl);
                assertThat(newContext.getCaption()).isEqualTo(caption);
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
            private RecipeCaption caption;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "test-video-id";
                videoUrl = URI.create("https://youtu.be/test");
                caption = mock(RecipeCaption.class);
            }

            @Test
            @DisplayName("Then - caption이 null이면 getCaption()은 null을 반환한다")
            void thenGetCaptionReturnsNullWhenCaptionIsNull() {
                RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

                assertThat(context.getCaption()).isNull();
            }

            @Test
            @DisplayName("Then - caption이 설정되면 getCaption()은 캡션을 반환한다")
            void thenGetCaptionReturnsCaptionWhenSet() {
                RecipeCreationExecutionContext context = RecipeCreationExecutionContext.from(
                        RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl),
                        caption);

                assertThat(context.getCaption()).isEqualTo(caption);
            }
        }
    }
}
