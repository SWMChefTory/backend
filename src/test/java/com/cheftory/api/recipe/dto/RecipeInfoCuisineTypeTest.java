package com.cheftory.api.recipe.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCuisineType Tests")
class RecipeInfoCuisineTypeTest {

  @Nested
  @DisplayName("fromString 메서드")
  class FromStringMethod {

    @Test
    @DisplayName("대문자 문자열로 유효한 타입을 변환한다")
    void shouldConvertValidTypeFromUppercaseString() {
      RecipeCuisineType result = RecipeCuisineType.fromString("KOREAN");

      assertThat(result).isEqualTo(RecipeCuisineType.KOREAN);
      assertThat(result.getKoreanName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("소문자 문자열로 유효한 타입을 변환한다")
    void shouldConvertValidTypeFromLowercaseString() {
      RecipeCuisineType result = RecipeCuisineType.fromString("korean");

      assertThat(result).isEqualTo(RecipeCuisineType.KOREAN);
      assertThat(result.getKoreanName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("혼합 대소문자 문자열로 유효한 타입을 변환한다")
    void shouldConvertValidTypeFromMixedCaseString() {
      RecipeCuisineType result = RecipeCuisineType.fromString("KoReAn");

      assertThat(result).isEqualTo(RecipeCuisineType.KOREAN);
      assertThat(result.getKoreanName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("모든 유효한 타입을 변환할 수 있다")
    void shouldConvertAllValidTypes() {
      assertThat(RecipeCuisineType.fromString("KOREAN")).isEqualTo(RecipeCuisineType.KOREAN);
      assertThat(RecipeCuisineType.fromString("SNACK")).isEqualTo(RecipeCuisineType.SNACK);
      assertThat(RecipeCuisineType.fromString("CHINESE")).isEqualTo(RecipeCuisineType.CHINESE);
      assertThat(RecipeCuisineType.fromString("JAPANESE")).isEqualTo(RecipeCuisineType.JAPANESE);
      assertThat(RecipeCuisineType.fromString("WESTERN")).isEqualTo(RecipeCuisineType.WESTERN);
      assertThat(RecipeCuisineType.fromString("DESSERT")).isEqualTo(RecipeCuisineType.DESSERT);
      assertThat(RecipeCuisineType.fromString("HEALTHY")).isEqualTo(RecipeCuisineType.HEALTHY);
      assertThat(RecipeCuisineType.fromString("BABY")).isEqualTo(RecipeCuisineType.BABY);
      assertThat(RecipeCuisineType.fromString("SIMPLE")).isEqualTo(RecipeCuisineType.SIMPLE);
    }

    @Test
    @DisplayName("존재하지 않는 타입 문자열은 예외를 던진다")
    void shouldThrowExceptionForInvalidTypeString() {
      assertThatThrownBy(() -> RecipeCuisineType.fromString("INVALID"))
          .isInstanceOf(RecipeException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.INVALID_CUISINE_TYPE);
    }

    @Test
    @DisplayName("빈 문자열은 예외를 던진다")
    void shouldThrowExceptionForEmptyString() {
      assertThatThrownBy(() -> RecipeCuisineType.fromString(""))
          .isInstanceOf(RecipeException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.INVALID_CUISINE_TYPE);
    }

    @Test
    @DisplayName("null 문자열은 예외를 던진다")
    void shouldThrowExceptionForNullString() {
      assertThatThrownBy(() -> RecipeCuisineType.fromString(null)).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("공백이 포함된 문자열은 예외를 던진다")
    void shouldThrowExceptionForStringWithSpaces() {
      assertThatThrownBy(() -> RecipeCuisineType.fromString("KOREAN "))
          .isInstanceOf(RecipeException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.INVALID_CUISINE_TYPE);
    }
  }

  @Nested
  @DisplayName("getKoreanName 메서드")
  class GetKoreanNameMethod {

    @Test
    @DisplayName("각 타입의 한국어 이름을 반환한다")
    void shouldReturnKoreanNameForEachType() {
      assertThat(RecipeCuisineType.KOREAN.getKoreanName()).isEqualTo("한식");
      assertThat(RecipeCuisineType.SNACK.getKoreanName()).isEqualTo("분식");
      assertThat(RecipeCuisineType.CHINESE.getKoreanName()).isEqualTo("중식");
      assertThat(RecipeCuisineType.JAPANESE.getKoreanName()).isEqualTo("일식");
      assertThat(RecipeCuisineType.WESTERN.getKoreanName()).isEqualTo("양식");
      assertThat(RecipeCuisineType.DESSERT.getKoreanName()).isEqualTo("디저트");
      assertThat(RecipeCuisineType.HEALTHY.getKoreanName()).isEqualTo("건강식");
      assertThat(RecipeCuisineType.BABY.getKoreanName()).isEqualTo("유아식");
      assertThat(RecipeCuisineType.SIMPLE.getKoreanName()).isEqualTo("간편식");
    }
  }
}
