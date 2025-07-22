package com.cheftory.api.recipeaccess;

import static org.mockito.Mockito.mock;

import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.RecipeService;
import com.cheftory.api.user.UserService;
import com.cheftory.api.utils.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("recipe access controller test")
public class RecipeAccessControllerTest extends RestDocsTest {
  private RecipeService recipeService;
  private RecipeAccessService recipeAccessService;
  private RecipeAccessController controller;
  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp(){
    recipeService = mock(RecipeService.class);
    recipeAccessService = mock(RecipeAccessService.class);
    controller = new RecipeAccessController(recipeAccessService);
    exceptionHandler = new GlobalExceptionHandler();

    mockMvc = mockMvcBuilder(controller)
        .withAdvice(exceptionHandler)
        .build();
  }


}
