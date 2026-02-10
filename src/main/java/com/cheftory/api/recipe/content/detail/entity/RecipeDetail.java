package com.cheftory.api.recipe.content.detail.entity;

import java.util.List;

/**
 * 레시피 상세 정보.
 *
 * @param description 레시피 설명
 * @param ingredients 재료 목록
 * @param tags 태그 목록
 * @param servings 인분
 * @param cookTime 조리 시간 (분)
 */
public record RecipeDetail(
        String description, List<Ingredient> ingredients, List<String> tags, Integer servings, Integer cookTime) {

    /**
     * 재료 정보.
     *
     * @param name 재료명
     * @param amount 수량
     * @param unit 단위
     */
    public record Ingredient(String name, Integer amount, String unit) {
        /**
         * 재료 정보를 생성합니다.
         *
         * @param name 재료명
         * @param amount 수량
         * @param unit 단위
         * @return 재료 정보
         */
        public static Ingredient of(String name, Integer amount, String unit) {
            return new Ingredient(name, amount, unit);
        }
    }

    /**
     * 레시피 상세 정보를 생성합니다.
     *
     * @param description 설명
     * @param ingredients 재료 목록
     * @param tags 태그 목록
     * @param servings 인분
     * @param cookTime 조리 시간
     * @return 레시피 상세 정보
     */
    public static RecipeDetail of(
            String description, List<Ingredient> ingredients, List<String> tags, Integer servings, Integer cookTime) {
        return new RecipeDetail(description, ingredients, tags, servings, cookTime);
    }
}
