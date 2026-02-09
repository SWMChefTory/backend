package com.cheftory.api.recipe.content.verify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.verify.client.RecipeVerifyClient;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeVerifyService")
class RecipeVerifyServiceTest {

    private RecipeVerifyClient recipeVerifyClient;
    private RecipeVerifyService recipeVerifyService;

    @BeforeEach
    void setUp() {
        recipeVerifyClient = mock(RecipeVerifyClient.class);
        recipeVerifyService = new RecipeVerifyService(recipeVerifyClient);
    }

    @Test
    @DisplayName("verify는 client 결과를 그대로 반환한다")
    void shouldReturnClientResponse() throws RecipeVerifyException {
        String videoId = "sample-video-id";
        RecipeVerifyClientResponse response = new RecipeVerifyClientResponse("s3://bucket/file.mp4", "video/mp4");

        doReturn(response).when(recipeVerifyClient).verifyVideo(videoId);

        RecipeVerifyClientResponse result = recipeVerifyService.verify(videoId);

        assertThat(result).isEqualTo(response);
        verify(recipeVerifyClient).verifyVideo(videoId);
    }
}
