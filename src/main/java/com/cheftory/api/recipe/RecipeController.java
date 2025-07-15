package com.cheftory.api.recipe;

import com.cheftory.api.recipe.dto.*;
import com.cheftory.api.recipe.helper.repository.RecipeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/v1/recipes")
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
    public RecipeOverviewsResponse getRecipeOverviewsResponse() {
        return recipeService
                .findRecipeOverviewsResponse();
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<RecipeNotFoundResponse> handle(RecipeNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RecipeNotFoundResponse
                        .from(exception.getMessage()));
    }
}
