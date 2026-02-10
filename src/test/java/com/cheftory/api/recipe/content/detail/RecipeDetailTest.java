package com.cheftory.api.recipe.content.detail;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeDetail 테스트")
class RecipeDetailTest {

    @Nested
    @DisplayName("레시피 상세 정보 생성 (of)")
    class Of {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {

            private String description;
            private List<RecipeDetail.Ingredient> ingredients;
            private List<String> tags;
            private Integer servings;
            private Integer cookTime;

            @BeforeEach
            void setUp() {
                description = "맛있는 김치찌개 만들기";
                ingredients = List.of(
                        RecipeDetail.Ingredient.of("김치", 200, "g"),
                        RecipeDetail.Ingredient.of("돼지고기", 150, "g"),
                        RecipeDetail.Ingredient.of("두부", 1, "모"),
                        RecipeDetail.Ingredient.of("대파", 1, "대"));
                tags = List.of("한식", "찌개", "김치", "매운맛");
                servings = 2;
                cookTime = 30;
            }

            @Nested
            @DisplayName("When - 레시피 상세 정보를 생성하면")
            class WhenCreateRecipeDetail {

                private RecipeDetail recipeDetail;

                @BeforeEach
                void setUp() {
                    recipeDetail = RecipeDetail.of(null, description, ingredients, tags, servings, cookTime);
                }

                @Test
                @DisplayName("Then - 레시피 상세 정보가 생성된다")
                void thenRecipeDetailIsCreated() {
                    assertThat(recipeDetail).isNotNull();
                    assertThat(recipeDetail.description()).isEqualTo("맛있는 김치찌개 만들기");
                    assertThat(recipeDetail.ingredients()).hasSize(4);
                    assertThat(recipeDetail.ingredients())
                            .containsExactly(
                                    RecipeDetail.Ingredient.of("김치", 200, "g"),
                                    RecipeDetail.Ingredient.of("돼지고기", 150, "g"),
                                    RecipeDetail.Ingredient.of("두부", 1, "모"),
                                    RecipeDetail.Ingredient.of("대파", 1, "대"));
                    assertThat(recipeDetail.tags()).hasSize(4);
                    assertThat(recipeDetail.tags()).containsExactly("한식", "찌개", "김치", "매운맛");
                    assertThat(recipeDetail.servings()).isEqualTo(2);
                    assertThat(recipeDetail.cookTime()).isEqualTo(30);
                }

                @Test
                @DisplayName("Then - 각 재료 정보가 올바르게 설정된다")
                void thenIngredientInfoIsCorrect() {
                    RecipeDetail.Ingredient firstIngredient =
                            recipeDetail.ingredients().get(0);
                    assertThat(firstIngredient.name()).isEqualTo("김치");
                    assertThat(firstIngredient.amount()).isEqualTo(200);
                    assertThat(firstIngredient.unit()).isEqualTo("g");

                    RecipeDetail.Ingredient lastIngredient =
                            recipeDetail.ingredients().get(3);
                    assertThat(lastIngredient.name()).isEqualTo("대파");
                    assertThat(lastIngredient.amount()).isEqualTo(1);
                    assertThat(lastIngredient.unit()).isEqualTo("대");
                }
            }
        }

        @Nested
        @DisplayName("Given - 간단한 레시피 파라미터가 주어졌을 때")
        class GivenSimpleRecipeParameters {

            private String description;
            private List<RecipeDetail.Ingredient> ingredients;
            private List<String> tags;
            private Integer servings;
            private Integer cookTime;

            @BeforeEach
            void setUp() {
                description = "간단한 계란찜";
                ingredients = List.of(RecipeDetail.Ingredient.of("계란", 3, "개"));
                tags = List.of("간식");
                servings = 1;
                cookTime = 5;
            }

            @Nested
            @DisplayName("When - 간단한 레시피를 생성하면")
            class WhenCreateSimpleRecipe {

                private RecipeDetail recipeDetail;

                @BeforeEach
                void setUp() {
                    recipeDetail = RecipeDetail.of(null, description, ingredients, tags, servings, cookTime);
                }

                @Test
                @DisplayName("Then - 간단한 레시피가 생성된다")
                void thenSimpleRecipeIsCreated() {
                    assertThat(recipeDetail).isNotNull();
                    assertThat(recipeDetail.description()).isEqualTo("간단한 계란찜");
                    assertThat(recipeDetail.ingredients()).hasSize(1);
                    assertThat(recipeDetail.ingredients().get(0).name()).isEqualTo("계란");
                    assertThat(recipeDetail.tags()).hasSize(1);
                    assertThat(recipeDetail.tags()).containsExactly("간식");
                    assertThat(recipeDetail.servings()).isEqualTo(1);
                    assertThat(recipeDetail.cookTime()).isEqualTo(5);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 목록들이 주어졌을 때")
        class GivenEmptyLists {

            private String description;
            private List<RecipeDetail.Ingredient> ingredients;
            private List<String> tags;
            private Integer servings;
            private Integer cookTime;

            @BeforeEach
            void setUp() {
                description = "재료 없는 레시피";
                ingredients = List.of();
                tags = List.of();
                servings = 0;
                cookTime = 0;
            }

            @Nested
            @DisplayName("When - 빈 목록으로 레시피를 생성하면")
            class WhenCreateRecipeWithEmptyLists {

                private RecipeDetail recipeDetail;

                @BeforeEach
                void setUp() {
                    recipeDetail = RecipeDetail.of(null, description, ingredients, tags, servings, cookTime);
                }

                @Test
                @DisplayName("Then - 빈 목록을 가진 레시피가 생성된다")
                void thenRecipeWithEmptyListsIsCreated() {
                    assertThat(recipeDetail).isNotNull();
                    assertThat(recipeDetail.description()).isEqualTo("재료 없는 레시피");
                    assertThat(recipeDetail.ingredients()).isEmpty();
                    assertThat(recipeDetail.tags()).isEmpty();
                    assertThat(recipeDetail.servings()).isEqualTo(0);
                    assertThat(recipeDetail.cookTime()).isEqualTo(0);
                }
            }
        }
    }

    @Nested
    @DisplayName("재료 생성 (Ingredient.of)")
    class IngredientOf {

        @Nested
        @DisplayName("Given - 유효한 재료 파라미터가 주어졌을 때")
        class GivenValidIngredientParameters {

            private String name;
            private Integer amount;
            private String unit;

            @BeforeEach
            void setUp() {
                name = "양파";
                amount = 1;
                unit = "개";
            }

            @Nested
            @DisplayName("When - 재료를 생성하면")
            class WhenCreateIngredient {

                private RecipeDetail.Ingredient ingredient;

                @BeforeEach
                void setUp() {
                    ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
                }

                @Test
                @DisplayName("Then - 재료가 생성된다")
                void thenIngredientIsCreated() {
                    assertThat(ingredient).isNotNull();
                    assertThat(ingredient.name()).isEqualTo("양파");
                    assertThat(ingredient.amount()).isEqualTo(1);
                    assertThat(ingredient.unit()).isEqualTo("개");
                }
            }
        }

        @Nested
        @DisplayName("Given - 그램 단위 재료 파라미터가 주어졌을 때")
        class GivenGramUnitIngredient {

            private String name;
            private Integer amount;
            private String unit;

            @BeforeEach
            void setUp() {
                name = "소금";
                amount = 5;
                unit = "g";
            }

            @Nested
            @DisplayName("When - 재료를 생성하면")
            class WhenCreateIngredient {

                private RecipeDetail.Ingredient ingredient;

                @BeforeEach
                void setUp() {
                    ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
                }

                @Test
                @DisplayName("Then - 그램 단위 재료가 생성된다")
                void thenGramIngredientIsCreated() {
                    assertThat(ingredient.name()).isEqualTo("소금");
                    assertThat(ingredient.amount()).isEqualTo(5);
                    assertThat(ingredient.unit()).isEqualTo("g");
                }
            }
        }

        @Nested
        @DisplayName("Given - 큰술 단위 재료 파라미터가 주어졌을 때")
        class GivenTablespoonUnitIngredient {

            private String name;
            private Integer amount;
            private String unit;

            @BeforeEach
            void setUp() {
                name = "올리브오일";
                amount = 2;
                unit = "큰술";
            }

            @Nested
            @DisplayName("When - 재료를 생성하면")
            class WhenCreateIngredient {

                private RecipeDetail.Ingredient ingredient;

                @BeforeEach
                void setUp() {
                    ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
                }

                @Test
                @DisplayName("Then - 큰술 단위 재료가 생성된다")
                void thenTablespoonIngredientIsCreated() {
                    assertThat(ingredient.name()).isEqualTo("올리브오일");
                    assertThat(ingredient.amount()).isEqualTo(2);
                    assertThat(ingredient.unit()).isEqualTo("큰술");
                }
            }
        }

        @Nested
        @DisplayName("Given - 컵 단위 재료 파라미터가 주어졌을 때")
        class GivenCupUnitIngredient {

            private String name;
            private Integer amount;
            private String unit;

            @BeforeEach
            void setUp() {
                name = "물";
                amount = 1;
                unit = "컵";
            }

            @Nested
            @DisplayName("When - 재료를 생성하면")
            class WhenCreateIngredient {

                private RecipeDetail.Ingredient ingredient;

                @BeforeEach
                void setUp() {
                    ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
                }

                @Test
                @DisplayName("Then - 컵 단위 재료가 생성된다")
                void thenCupIngredientIsCreated() {
                    assertThat(ingredient.name()).isEqualTo("물");
                    assertThat(ingredient.amount()).isEqualTo(1);
                    assertThat(ingredient.unit()).isEqualTo("컵");
                }
            }
        }

        @Nested
        @DisplayName("Given - 0 수량 재료 파라미터가 주어졌을 때")
        class GivenZeroAmountIngredient {

            private String name;
            private Integer amount;
            private String unit;

            @BeforeEach
            void setUp() {
                name = "장식용 파슬리";
                amount = 0;
                unit = "조금";
            }

            @Nested
            @DisplayName("When - 재료를 생성하면")
            class WhenCreateIngredient {

                private RecipeDetail.Ingredient ingredient;

                @BeforeEach
                void setUp() {
                    ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
                }

                @Test
                @DisplayName("Then - 0 수량 재료가 생성된다")
                void thenZeroAmountIngredientIsCreated() {
                    assertThat(ingredient.name()).isEqualTo("장식용 파슬리");
                    assertThat(ingredient.amount()).isEqualTo(0);
                    assertThat(ingredient.unit()).isEqualTo("조금");
                }
            }
        }

        @Nested
        @DisplayName("Given - 큰 수량 재료 파라미터가 주어졌을 때")
        class GivenLargeAmountIngredient {

            private String name;
            private Integer amount;
            private String unit;

            @BeforeEach
            void setUp() {
                name = "쌀";
                amount = 1000;
                unit = "g";
            }

            @Nested
            @DisplayName("When - 재료를 생성하면")
            class WhenCreateIngredient {

                private RecipeDetail.Ingredient ingredient;

                @BeforeEach
                void setUp() {
                    ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
                }

                @Test
                @DisplayName("Then - 큰 수량 재료가 생성된다")
                void thenLargeAmountIngredientIsCreated() {
                    assertThat(ingredient.name()).isEqualTo("쌀");
                    assertThat(ingredient.amount()).isEqualTo(1000);
                    assertThat(ingredient.unit()).isEqualTo("g");
                }
            }
        }
    }
}
