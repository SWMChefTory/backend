package com.cheftory.api.recipe;

import com.cheftory.api.recipe.dto.*;
import com.cheftory.api.recipe.exception.RecipeCreationPendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping("")
    public RecipeCreateResponse createRecipe(@RequestBody RecipeCreateRequest recipeCreateRequest) {
        UUID recipeId = recipeService.create(recipeCreateRequest.toUrl());
        return RecipeCreateResponse.successFrom(recipeId);
    }

    @GetMapping("/{recipeId}")
    public RecipeFindResponse getRecipe(@PathVariable UUID recipeId) {
        return recipeService
                .findTotalRecipeInfo(recipeId);
    }

    @GetMapping("")
    public RecipeOverviewsResponse getRecipeOverviewsResponse(UUID recipeId) {
        return recipeService
                .findRecipeOverviewsResponse();
    }

    @ExceptionHandler(RecipeCreationPendingException.class)
    public ResponseEntity<RecipeFindPendingResponse> handle(RecipeCreationPendingException exception) {
        log.info("Recipe creation pending exception: {}", exception.getMessage());
        RecipeFindPendingResponse body = RecipeFindPendingResponse
                .from(exception.getState());
        return ResponseEntity
                .status(HttpStatus.ACCEPTED) // 202 응답
                .body(body);
    }
}
