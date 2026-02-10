package com.cheftory.api.voicecommand;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseErrorFields;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseSuccessFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

import com.cheftory.api.exception.GlobalErrorCode;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.utils.RestDocsTest;
import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("VoiceCommandHistoryController 테스트")
public class VoiceCommandHistoryControllerTest extends RestDocsTest {
    private VoiceCommandHistoryService voiceCommandHistoryservice;
    private UserService userService;
    private VoiceCommandHistoryController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        exceptionHandler = new GlobalExceptionHandler();

        voiceCommandHistoryservice = mock(VoiceCommandHistoryService.class);
        controller = new VoiceCommandHistoryController(voiceCommandHistoryservice);

        mockMvc = mockMvcBuilder(controller)
                .withValidator(UserService.class, userService)
                .withAdvice(exceptionHandler)
                .build();
    }

    @Nested
    @DisplayName("음성 명령 기록 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 요청일 때")
        class GivenValidRequest {
            String testBaseIntent;
            String testIntent;
            UUID userId;
            String testSttModel;
            String testIntentModel;
            Integer start;
            Integer end;
            Map<String, Object> request;

            @BeforeEach
            void setUp() throws VoiceCommandHistoryException {
                testBaseIntent = "testBaseIntent";
                testIntent = "testIntent";
                userId = generateUserId();
                testSttModel = "VITO";
                testIntentModel = "gpt-4";
                start = 1;
                end = 2;

                request = Map.of(
                        "transcribe", testBaseIntent,
                        "intent", testIntent,
                        "user_id", userId.toString(),
                        "stt_model", testSttModel,
                        "intent_model", testIntentModel,
                        "start", start,
                        "end", end);

                doReturn(true).when(userService).exists(any(UUID.class));
                doNothing()
                        .when(voiceCommandHistoryservice)
                        .create(
                                anyString(),
                                anyString(),
                                any(UUID.class),
                                anyString(),
                                anyString(),
                                anyInt(),
                                anyInt());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .post("/papi/v1/voice-command")
                            .then();
                }

                @Test
                @DisplayName("Then - 성공 응답을 반환한다")
                void thenReturnsSuccess() throws VoiceCommandHistoryException {
                    response.status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(VoiceCommandHistoryControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(
                                            fieldWithPath("transcribe").description("기본 의도"),
                                            fieldWithPath("intent").description("의도"),
                                            fieldWithPath("user_id").description("유저 ID"),
                                            fieldWithPath("start").description("발화의 시작 시간"),
                                            fieldWithPath("end").description("발화의 종료 시간"),
                                            enumFields("stt_model", "음성 인식에 사용되는 모델. 사용 가능한 값: ", STTModel.class),
                                            enumFields(
                                                    "intent_model", "의도 분류에 사용되는 모델. 사용 가능한 값: ", IntentModel.class)),
                                    responseSuccessFields()));

                    assertSuccessResponse(response);
                    verify(voiceCommandHistoryservice)
                            .create(testBaseIntent, testIntent, userId, testSttModel, testIntentModel, start, end);
                    verify(userService).exists(userId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 사용자일 때")
        class GivenNonExistingUser {
            Map<String, Object> request;

            @BeforeEach
            void setUp() {
                request = Map.of(
                        "transcribe", "test",
                        "intent", "test",
                        "user_id", generateUserId().toString(),
                        "stt_model", "VITO",
                        "intent_model", "gpt-4",
                        "start", 1,
                        "end", 2);

                doReturn(false).when(userService).exists(any(UUID.class));
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .post("/papi/v1/voice-command")
                            .then();
                }

                @Test
                @DisplayName("Then - USER_NOT_FOUND 에러를 반환한다")
                void thenReturnsError() {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(VoiceCommandHistoryControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    responseErrorFields(UserErrorCode.USER_NOT_FOUND)));

                    assertErrorResponse(response, UserErrorCode.USER_NOT_FOUND);
                }
            }
        }

        @Nested
        @DisplayName("Given - 필수 필드가 누락되었을 때")
        class GivenMissingFields {
            Map<String, Object> request;

            @BeforeEach
            void setUp() {
                request = Map.of(
                        "base_intent",
                        "testBaseIntent",
                        "user_id",
                        generateUserId().toString());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .post("/papi/v1/voice-command")
                            .then();
                }

                @Test
                @DisplayName("Then - FIELD_REQUIRED 에러를 반환한다")
                void thenReturnsError() {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(VoiceCommandHistoryControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    responseErrorFields(GlobalErrorCode.FIELD_REQUIRED)));
                }
            }
        }

        @Nested
        @DisplayName("Given - 지원하지 않는 STT 모델일 때")
        class GivenUnsupportedSttModel {
            Map<String, Object> request;
            UUID userId;

            @BeforeEach
            void setUp() throws VoiceCommandHistoryException {
                userId = generateUserId();
                request = Map.of(
                        "transcribe", "test",
                        "intent", "test",
                        "user_id", userId.toString(),
                        "stt_model", "INVALID",
                        "intent_model", "gpt-4",
                        "start", 1,
                        "end", 2);

                doReturn(true).when(userService).exists(any(UUID.class));
                doThrow(new VoiceCommandHistoryException(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL))
                        .when(voiceCommandHistoryservice)
                        .create(
                                anyString(),
                                anyString(),
                                any(UUID.class),
                                anyString(),
                                anyString(),
                                anyInt(),
                                anyInt());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .post("/papi/v1/voice-command")
                            .then();
                }

                @Test
                @DisplayName("Then - UNKNOWN_STT_MODEL 에러를 반환한다")
                void thenReturnsError() {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(VoiceCommandHistoryControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    responseErrorFields(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL)));

                    assertErrorResponse(response, VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL);
                }
            }
        }

        @Nested
        @DisplayName("Given - 지원하지 않는 Intent 모델일 때")
        class GivenUnsupportedIntentModel {
            Map<String, Object> request;
            UUID userId;

            @BeforeEach
            void setUp() throws VoiceCommandHistoryException {
                userId = generateUserId();
                request = Map.of(
                        "transcribe", "test",
                        "intent", "test",
                        "user_id", userId.toString(),
                        "stt_model", "VITO",
                        "intent_model", "INVALID",
                        "start", 1,
                        "end", 2);

                doReturn(true).when(userService).exists(any(UUID.class));
                doThrow(new VoiceCommandHistoryException(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL))
                        .when(voiceCommandHistoryservice)
                        .create(
                                anyString(),
                                anyString(),
                                any(UUID.class),
                                anyString(),
                                anyString(),
                                anyInt(),
                                anyInt());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .post("/papi/v1/voice-command")
                            .then();
                }

                @Test
                @DisplayName("Then - UNKNOWN_INTENT_MODEL 에러를 반환한다")
                void thenReturnsError() {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(VoiceCommandHistoryControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    responseErrorFields(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL)));

                    assertErrorResponse(response, VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL);
                }
            }
        }
    }

    private UUID generateUserId() {
        return UUID.randomUUID();
    }
}
