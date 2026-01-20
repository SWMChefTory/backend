package com.cheftory.api.search.autocomplete;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("RecipeAutocomplete Controller")
public class AutocompleteControllerTest extends RestDocsTest {

    private AutocompleteService autocompleteService;
    private AutocompleteController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        autocompleteService = mock(AutocompleteService.class);
        controller = new AutocompleteController(autocompleteService);
        exceptionHandler = new GlobalExceptionHandler();

        mockMvc = mockMvcBuilder(controller).withAdvice(exceptionHandler).build();
    }

    @Nested
    @DisplayName("자동완성 검색")
    class GetAutocomplete {

        @Nested
        @DisplayName("Given - 유효한 검색어가 주어졌을 때")
        class GivenValidQuery {

            private String query;
            private List<Autocomplete> autocompletes;

            @BeforeEach
            void setUp() {
                query = "김치";

                Autocomplete autocomplete1 = mock(Autocomplete.class);
                Autocomplete autocomplete2 = mock(Autocomplete.class);
                Autocomplete autocomplete3 = mock(Autocomplete.class);

                doReturn("김치찌개").when(autocomplete1).getText();
                doReturn("김치전").when(autocomplete2).getText();
                doReturn("김치볶음밥").when(autocomplete3).getText();

                autocompletes = List.of(autocomplete1, autocomplete2, autocomplete3);

                doReturn(autocompletes)
                        .when(autocompleteService)
                        .autocomplete(any(AutocompleteScope.class), any(String.class));
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 자동완성 목록을 성공적으로 반환해야 한다")
                void thenShouldReturnAutocompleteList() {
                    var response = given().contentType(ContentType.JSON)
                            .param("query", query)
                            .get("/api/v1/search/autocomplete")
                            .then()
                            .status(HttpStatus.OK)
                            .body("autocompletes", hasSize(3))
                            .apply(document(
                                    getNestedClassPath(this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    queryParameters(
                                            parameterWithName("query").description("자동완성 검색어"),
                                            parameterWithName("scope")
                                                    .description("검색 범위")
                                                    .optional()),
                                    responseFields(
                                            fieldWithPath("autocompletes").description("자동완성 목록"),
                                            fieldWithPath("autocompletes[].autocomplete")
                                                    .description("자동완성 텍스트"))));

                    verify(autocompleteService).autocomplete(AutocompleteScope.RECIPE, query);

                    var responseBody = response.extract().jsonPath();
                    var autocompleteList = responseBody.getList("autocompletes");
                    assertThat(autocompleteList).hasSize(3);
                }
            }
        }

        @Nested
        @DisplayName("Given - 자동완성 결과가 없는 검색어가 주어졌을 때")
        class GivenQueryWithNoResults {

            private String query;

            @BeforeEach
            void setUp() {
                query = "존재하지않는검색어";
                doReturn(List.of())
                        .when(autocompleteService)
                        .autocomplete(any(AutocompleteScope.class), any(String.class));
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 빈 목록을 반환해야 한다")
                void thenShouldReturnEmptyList() {
                    given().contentType(ContentType.JSON)
                            .param("query", query)
                            .get("/api/v1/search/autocomplete")
                            .then()
                            .status(HttpStatus.OK)
                            .body("autocompletes", hasSize(0));

                    verify(autocompleteService).autocomplete(AutocompleteScope.RECIPE, query);
                }
            }
        }

        @Nested
        @DisplayName("Given - 일부 일치하는 검색어가 주어졌을 때")
        class GivenPartialMatchQuery {

            private String query;
            private List<Autocomplete> autocompletes;

            @BeforeEach
            void setUp() {
                query = "파";

                Autocomplete autocomplete1 = mock(Autocomplete.class);
                Autocomplete autocomplete2 = mock(Autocomplete.class);

                doReturn("파스타").when(autocomplete1).getText();
                doReturn("파김치").when(autocomplete2).getText();

                autocompletes = List.of(autocomplete1, autocomplete2);

                doReturn(autocompletes)
                        .when(autocompleteService)
                        .autocomplete(any(AutocompleteScope.class), any(String.class));
            }

            @Nested
            @DisplayName("When - 자동완성을 요청한다면")
            class WhenRequestingAutocomplete {

                @Test
                @DisplayName("Then - 일치하는 자동완성 목록을 반환해야 한다")
                void thenShouldReturnMatchingAutocompleteList() {
                    var response = given().contentType(ContentType.JSON)
                            .param("query", query)
                            .get("/api/v1/search/autocomplete")
                            .then()
                            .status(HttpStatus.OK)
                            .body("autocompletes", hasSize(2));

                    verify(autocompleteService).autocomplete(AutocompleteScope.RECIPE, query);

                    var responseBody = response.extract().jsonPath();
                    assertThat(responseBody.getList("autocompletes")).hasSize(2);
                }
            }
        }
    }
}
