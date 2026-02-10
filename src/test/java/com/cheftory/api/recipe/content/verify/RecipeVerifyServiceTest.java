package com.cheftory.api.recipe.content.verify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.verify.client.RecipeVerifyExternalClient;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeVerifyService 테스트")
class RecipeVerifyServiceTest {

    private RecipeVerifyExternalClient recipeVerifyExternalClient;
    private RecipeVerifyService recipeVerifyService;

    @BeforeEach
    void setUp() {
        recipeVerifyExternalClient = mock(RecipeVerifyExternalClient.class);
        recipeVerifyService = new RecipeVerifyService(recipeVerifyExternalClient);
    }

    @Nested
    @DisplayName("비디오 검증 (verify)")
    class Verify {

        @Nested
        @DisplayName("Given - 비디오 ID가 주어졌을 때")
        class GivenVideoId {
            String videoId;
            RecipeVerifyClientResponse response;

            @BeforeEach
            void setUp() throws RecipeVerifyException {
                videoId = "sample-video-id";
                response = new RecipeVerifyClientResponse("s3://bucket/file.mp4", "video/mp4");
                doReturn(response).when(recipeVerifyExternalClient).verify(videoId);
            }

            @Nested
            @DisplayName("When - 검증을 요청하면")
            class WhenVerifying {
                RecipeVerifyClientResponse result;

                @BeforeEach
                void setUp() throws RecipeVerifyException {
                    result = recipeVerifyService.verify(videoId);
                }

                @Test
                @DisplayName("Then - 클라이언트 응답을 반환한다")
                void thenReturnsResponse() throws RecipeVerifyException {
                    assertThat(result).isEqualTo(response);
                    verify(recipeVerifyExternalClient).verify(videoId);
                }
            }
        }
    }
}
