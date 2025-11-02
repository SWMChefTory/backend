package com.cheftory.api.recipeinfo.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeInfoCuisineType Tests")
class RecipeInfoCuisineTypeTest {

  @Nested
  @DisplayName("fromString 메서드")
  class FromStringMethod {

    @Test
    @DisplayName("대문자 문자열로 유효한 타입을 변환한다")
    void shouldConvertValidTypeFromUppercaseString() {
      RecipeInfoCuisineType result = RecipeInfoCuisineType.fromString("KOREAN");

      assertThat(result).isEqualTo(RecipeInfoCuisineType.KOREAN);
      assertThat(result.getKoreanName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("소문자 문자열로 유효한 타입을 변환한다")
    void shouldConvertValidTypeFromLowercaseString() {
      RecipeInfoCuisineType result = RecipeInfoCuisineType.fromString("korean");

      assertThat(result).isEqualTo(RecipeInfoCuisineType.KOREAN);
      assertThat(result.getKoreanName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("혼합 대소문자 문자열로 유효한 타입을 변환한다")
    void shouldConvertValidTypeFromMixedCaseString() {
      RecipeInfoCuisineType result = RecipeInfoCuisineType.fromString("KoReAn");

      assertThat(result).isEqualTo(RecipeInfoCuisineType.KOREAN);
      assertThat(result.getKoreanName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("모든 유효한 타입을 변환할 수 있다")
    void shouldConvertAllValidTypes() {
      assertThat(RecipeInfoCuisineType.fromString("KOREAN")).isEqualTo(RecipeInfoCuisineType.KOREAN);
      assertThat(RecipeInfoCuisineType.fromString("SNACK")).isEqualTo(RecipeInfoCuisineType.SNACK);
      assertThat(RecipeInfoCuisineType.fromString("CHINESE")).isEqualTo(RecipeInfoCuisineType.CHINESE);
      assertThat(RecipeInfoCuisineType.fromString("JAPANESE")).isEqualTo(RecipeInfoCuisineType.JAPANESE);
      assertThat(RecipeInfoCuisineType.fromString("WESTERN")).isEqualTo(RecipeInfoCuisineType.WESTERN);
      assertThat(RecipeInfoCuisineType.fromString("DESSERT")).isEqualTo(RecipeInfoCuisineType.DESSERT);
      assertThat(RecipeInfoCuisineType.fromString("HEALTHY")).isEqualTo(RecipeInfoCuisineType.HEALTHY);
      assertThat(RecipeInfoCuisineType.fromString("BABY")).isEqualTo(RecipeInfoCuisineType.BABY);
      assertThat(RecipeInfoCuisineType.fromString("SIMPLE")).isEqualTo(RecipeInfoCuisineType.SIMPLE);
    }

    @Test
    @DisplayName("존재하지 않는 타입 문자열은 예외를 던진다")
    void shouldThrowExceptionForInvalidTypeString() {
      assertThatThrownBy(() -> RecipeInfoCuisineType.fromString("INVALID"))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.INVALID_CUISINE_TYPE);
    }

    @Test
    @DisplayName("빈 문자열은 예외를 던진다")
    void shouldThrowExceptionForEmptyString() {
      assertThatThrownBy(() -> RecipeInfoCuisineType.fromString(""))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.INVALID_CUISINE_TYPE);
    }

    @Test
    @DisplayName("null 문자열은 예외를 던진다")
    void shouldThrowExceptionForNullString() {
      assertThatThrownBy(() -> RecipeInfoCuisineType.fromString(null))
          .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("공백이 포함된 문자열은 예외를 던진다")
    void shouldThrowExceptionForStringWithSpaces() {
      assertThatThrownBy(() -> RecipeInfoCuisineType.fromString("KOREAN "))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.INVALID_CUISINE_TYPE);
    }
  }

  @Nested
  @DisplayName("getKoreanName 메서드")
  class GetKoreanNameMethod {

    @Test
    @DisplayName("각 타입의 한국어 이름을 반환한다")
    void shouldReturnKoreanNameForEachType() {
      assertThat(RecipeInfoCuisineType.KOREAN.getKoreanName()).isEqualTo("한식");
      assertThat(RecipeInfoCuisineType.SNACK.getKoreanName()).isEqualTo("분식");
      assertThat(RecipeInfoCuisineType.CHINESE.getKoreanName()).isEqualTo("중식");
      assertThat(RecipeInfoCuisineType.JAPANESE.getKoreanName()).isEqualTo("일식");
      assertThat(RecipeInfoCuisineType.WESTERN.getKoreanName()).isEqualTo("양식");
      assertThat(RecipeInfoCuisineType.DESSERT.getKoreanName()).isEqualTo("디저트");
      assertThat(RecipeInfoCuisineType.HEALTHY.getKoreanName()).isEqualTo("건강식");
      assertThat(RecipeInfoCuisineType.BABY.getKoreanName()).isEqualTo("유아식");
      assertThat(RecipeInfoCuisineType.SIMPLE.getKoreanName()).isEqualTo("간편식");
    }
  }
}
