package com.cheftory.api.auth.verifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.auth.verifier.client.GoogleTokenClient;
import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("GoogleTokenVerifier 테스트")
class GoogleTokenVerifierTest {

    private MockWebServer mockWebServer;
    private GoogleTokenVerifier googleTokenVerifier;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

        GoogleTokenClient mockClient = new GoogleTokenClient() {
            @Override
            public JsonNode fetchTokenInfo(String idToken) throws VerificationException {
                try {
                    String response = webClient
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/")
                                    .queryParam("id_token", idToken)
                                    .build())
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    if (response == null) {
                        throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
                    }

                    return mapper.readTree(response);
                } catch (Exception e) {
                    throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
                }
            }
        };

        googleTokenVerifier = new GoogleTokenVerifier(mockClient);
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Nested
    @DisplayName("getSubFromToken 메서드")
    class GetSubFromToken {

        @Nested
        @DisplayName("Given - 유효한 토큰일 때")
        class GivenValidToken {

            @BeforeEach
            void setUp() {
                ObjectNode responseBody = mapper.createObjectNode();
                responseBody.put("sub", "google-user-123");
                responseBody.put("email", "test@gmail.com");
                responseBody.put("name", "Test User");

                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(200)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(responseBody.toString())
                        .build());
            }

            @Nested
            @DisplayName("When - sub 추출을 요청하면")
            class WhenExtractingSub {

                @Test
                @DisplayName("Then - sub를 반환한다")
                void thenReturnsSub() throws VerificationException {
                    String result = googleTokenVerifier.getSubFromToken("valid-id-token");

                    assertThat(result).isEqualTo("google-user-123");
                }
            }
        }

        @Nested
        @DisplayName("Given - sub가 없는 응답일 때")
        class GivenMissingSub {

            @BeforeEach
            void setUp() {
                ObjectNode responseBody = mapper.createObjectNode();
                responseBody.put("email", "test@gmail.com");
                responseBody.put("name", "Test User");

                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(200)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(responseBody.toString())
                        .build());
            }

            @Nested
            @DisplayName("When - sub 추출을 요청하면")
            class WhenExtractingSub {

                @Test
                @DisplayName("Then - GOOGLE_MISSING_SUB 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> googleTokenVerifier.getSubFromToken("token-without-sub"))
                            .isInstanceOf(VerificationException.class)
                            .extracting(e -> ((VerificationException) e).getError())
                            .isEqualTo(VerificationErrorCode.GOOGLE_MISSING_SUB);
                }
            }
        }

        @Nested
        @DisplayName("Given - sub가 null인 응답일 때")
        class GivenNullSub {

            @BeforeEach
            void setUp() {
                ObjectNode responseBody = mapper.createObjectNode();
                responseBody.putNull("sub");
                responseBody.put("email", "test@gmail.com");

                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(200)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(responseBody.toString())
                        .build());
            }

            @Nested
            @DisplayName("When - sub 추출을 요청하면")
            class WhenExtractingSub {

                @Test
                @DisplayName("Then - GOOGLE_MISSING_SUB 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> googleTokenVerifier.getSubFromToken("token-with-null-sub"))
                            .isInstanceOf(VerificationException.class)
                            .extracting(e -> ((VerificationException) e).getError())
                            .isEqualTo(VerificationErrorCode.GOOGLE_MISSING_SUB);
                }
            }
        }

        @Nested
        @DisplayName("Given - 400 에러 응답일 때")
        class GivenErrorResponse {

            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(new MockResponse.Builder()
                        .code(400)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"error\": \"invalid_token\"}")
                        .build());
            }

            @Nested
            @DisplayName("When - sub 추출을 요청하면")
            class WhenExtractingSub {

                @Test
                @DisplayName("Then - GOOGLE_RESPONSE_NOT_OK 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> googleTokenVerifier.getSubFromToken("invalid-token"))
                            .isInstanceOf(VerificationException.class)
                            .extracting(e -> ((VerificationException) e).getError())
                            .isEqualTo(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
                }
            }
        }

        @Nested
        @DisplayName("Given - 500 에러 응답일 때")
        class GivenServerError {

            @BeforeEach
            void setUp() {
                mockWebServer.enqueue(new MockResponse.Builder().code(500).build());
            }

            @Nested
            @DisplayName("When - sub 추출을 요청하면")
            class WhenExtractingSub {

                @Test
                @DisplayName("Then - GOOGLE_RESPONSE_NOT_OK 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> googleTokenVerifier.getSubFromToken("token"))
                            .isInstanceOf(VerificationException.class)
                            .extracting(e -> ((VerificationException) e).getError())
                            .isEqualTo(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
                }
            }
        }
    }
}
