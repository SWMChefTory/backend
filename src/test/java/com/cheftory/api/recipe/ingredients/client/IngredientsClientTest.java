package com.cheftory.api.recipe.ingredients.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.ingredients.client.dto.ClientIngredientsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class IngredientsClientTest {
  @InjectMocks
  private RecipeIngredientsClient recipeIngredientsClient;
  @Mock
  private WebClient webClient;
  @Mock
  private ExchangeFunction mockExchange;

  @Nested
  @DisplayName("Given : 유효한 id가 들어올 때")
  class GivenValidVideoId {

    private String jsonResponse;
    private ClientIngredientsResponse response;
    private CaptionInfo captionInfo;

    @BeforeEach
    void setUp() throws Exception {
      jsonResponse = mock(String.class);
      response = mock(ClientIngredientsResponse.class);
      captionInfo = mock(CaptionInfo.class);
    }

    @Nested
    @DisplayName("When : 재료에 대해 생성한다면")
    class WhenFetchCaption {

      @BeforeEach
      void setUp() {
        ClientResponse mockResponse = ClientResponse.create(org.springframework.http.HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(jsonResponse)
            .build();


        when(mockExchange.exchange(any(ClientRequest.class)))
            .thenReturn(Mono.just(mockResponse));
      }

//      @Test
//      @DisplayName("Then : 성공적으로 자막 정보를 받아온다.")
//      void then() {
//        ClientCaptionResponse resp = captionClient.fetchCaption(videoId);
//        assertNotNull(resp);
//      }
    }
  }

}
