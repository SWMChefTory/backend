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
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("AutocompleteController 테스트")
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
    @DisplayName("자동완성 검색 (getAutocomplete)")
    class GetAutocomplete {

        @Nested
        @DisplayName("Given - 유효한 검색어가 주어졌을 때")
        class GivenValidQuery {
            String query;
            List<Autocomplete> autocompletes;

            @BeforeEach
            void setUp() throws SearchException {
                query = "김치";
                Autocomplete a1 = mock(Autocomplete.class);
                Autocomplete a2 = mock(Autocomplete.class);
                Autocomplete a3 = mock(Autocomplete.class);

                doReturn("김치찌개").when(a1).getText();
                doReturn("김치전").when(a2).getText();
                doReturn("김치볶음밥").when(a3).getText();

                autocompletes = List.of(a1, a2, a3);
                doReturn(autocompletes)
                        .when(autocompleteService)
                        .autocomplete(any(AutocompleteScope.class), any(String.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenRequesting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .param("query", query)
                            .get("/api/v1/search/autocomplete")
                            .then();
                }

                @Test
                @DisplayName("Then - 자동완성 목록을 반환한다")
                void thenReturnsList() throws SearchException {
                    response.status(HttpStatus.OK)
                            .body("autocompletes", hasSize(3))
                            .apply(document(
                                    getNestedClassPath(AutocompleteControllerTest.this.getClass()) + "/{method-name}",
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

                    var body = response.extract().jsonPath();
                    assertThat(body.getList("autocompletes")).hasSize(3);
                }
            }
        }

        @Nested
        @DisplayName("Given - 결과가 없는 검색어일 때")
        class GivenNoResults {
            String query;

            @BeforeEach
            void setUp() throws SearchException {
                query = "없는검색어";
                doReturn(List.of())
                        .when(autocompleteService)
                        .autocomplete(any(AutocompleteScope.class), any(String.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenRequesting {

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() throws SearchException {
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
        @DisplayName("Given - 일부 일치하는 검색어일 때")
        class GivenPartialMatch {
            String query;
            List<Autocomplete> autocompletes;

            @BeforeEach
            void setUp() throws SearchException {
                query = "파";
                Autocomplete a1 = mock(Autocomplete.class);
                Autocomplete a2 = mock(Autocomplete.class);
                doReturn("파스타").when(a1).getText();
                doReturn("파김치").when(a2).getText();
                autocompletes = List.of(a1, a2);

                doReturn(autocompletes)
                        .when(autocompleteService)
                        .autocomplete(any(AutocompleteScope.class), any(String.class));
            }

            @Nested
            @DisplayName("When - 검색을 요청하면")
            class WhenRequesting {

                @Test
                @DisplayName("Then - 일치하는 목록을 반환한다")
                void thenReturnsMatching() throws SearchException {
                    var response = given().contentType(ContentType.JSON)
                            .param("query", query)
                            .get("/api/v1/search/autocomplete")
                            .then()
                            .status(HttpStatus.OK)
                            .body("autocompletes", hasSize(2));

                    verify(autocompleteService).autocomplete(AutocompleteScope.RECIPE, query);

                    var body = response.extract().jsonPath();
                    assertThat(body.getList("autocompletes")).hasSize(2);
                }
            }
        }
    }
}
