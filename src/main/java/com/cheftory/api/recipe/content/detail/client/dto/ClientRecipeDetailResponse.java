package com.cheftory.api.recipe.content.detail.client.dto;

import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 레시피 상세 생성 외부 API 응답 DTO
 *
 * <p>외부 레시피 상세 생성 API로부터 받은 상세 정보 응답을 담습니다.</p>
 *
 * @param title AI 생성 레시피 제목
 * @param description 레시피 설명
 * @param ingredients 재료 목록
 * @param tags 태그 목록
 * @param servings 인분
 * @param cookTime 조리 시간 (분)
 */
public record ClientRecipeDetailResponse(
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("ingredients") List<Ingredient> ingredients,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("cook_time") Integer cookTime) {
    /**
     * 재료 정보 레코드
     *
     * @param name 재료 이름
     * @param amount 양
     * @param unit 단위
     */
    public record Ingredient(
            @JsonProperty("name") String name,
            @JsonProperty("amount") Integer amount,
            @JsonProperty("unit") String unit) {}

    /**
     * 응답 DTO를 RecipeDetail 엔티티로 변환
     *
     * @return 레시피 상세 엔티티
     */
    public RecipeDetail toRecipeDetail() {
        List<RecipeDetail.Ingredient> recipeIngredients = this.ingredients.stream()
                .map(ing -> RecipeDetail.Ingredient.of(ing.name, ing.amount, ing.unit))
                .toList();
        return RecipeDetail.of(this.title, this.description, recipeIngredients, this.tags, this.servings, this.cookTime);
    }
}
