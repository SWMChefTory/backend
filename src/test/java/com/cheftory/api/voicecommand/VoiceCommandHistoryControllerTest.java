package com.cheftory.api.voicecommand;
import com.cheftory.api.exception.GlobalErrorCode;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseErrorFields;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseSuccessFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;

@DisplayName("VoiceCommand Controller")
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
    @DisplayName("Given - 유효한 음성 명령 요청이 주어졌을 때")
    class GivenValidVoiceCommandRequest {

        private String testBaseIntent;
        private String testIntent;
        private UUID userId;
        private String testSttModel;
        private String testIntentModel;
        private Map<String, Object> request;

        @BeforeEach
        void setUp() {
            testBaseIntent = "testBaseIntent";
            testIntent = "testIntent";
            userId = generateUserId();
            testSttModel = "VITO";
            testIntentModel = "gpt-4";

            request = Map.of(
                "transcribe", testBaseIntent,
                "intent", testIntent,
                "user_id", userId.toString(),
                "stt_model", testSttModel,
                "intent_model", testIntentModel
            );
        }

        @Nested
        @DisplayName("When - 음성 명령을 생성할 때")
        class WhenCreatingVoiceCommand {

            @BeforeEach
            void setUp() {
                when(userService.exists(userId)).thenReturn(true);
                doNothing()
                    .when(voiceCommandHistoryservice)
                    .create(testBaseIntent, testIntent, userId, testSttModel, testIntentModel);
            }

            @Test
            @DisplayName("Then - 음성 명령 기록을 성공적으로 생성한다")
            public void thenShouldCreateVoiceCommandHistory() {
                var response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/papi/v1/voice-command")
                    .then()
                    .status(HttpStatus.OK)
                    .apply(
                        document(
                            getNestedClassPath(this.getClass())+"/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            requestFields(
                                fieldWithPath("transcribe").description("기본 의도"),
                                fieldWithPath("intent").description("의도"),
                                fieldWithPath("user_id").description("유저 ID"),
                                enumFields(
                                    "stt_model",
                                    "음성 인식에 사용되는 모델. 사용 가능한 값: ",
                                    STTModel.class
                                ),
                                enumFields(
                                    "intent_model",
                                    "의도 분류에 사용되는 모델. 사용 가능한 값: ",
                                    IntentModel.class
                                )
                            ),
                            responseSuccessFields()
                        )
                    );

                assertSuccessResponse(response);
                verify(voiceCommandHistoryservice).create(testBaseIntent, testIntent, userId, testSttModel, testIntentModel);
                verify(userService).exists(userId);
            }
        }
    }

    @Nested
    @DisplayName("Given - 잘못된 요청이 주어졌을 때")
    class GivenInvalidRequest {

        @Nested
        @DisplayName("When - 존재하지 않는 유저 ID로 요청할 때")
        class WhenRequestWithNonExistentUserId {

            private String testBaseIntent;
            private String testIntent;
            private UUID userId;
            private String testSttModel;
            private String testIntentModel;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                testBaseIntent = "testBaseIntent";
                testIntent = "testIntent";
                userId = generateUserId();
                testSttModel = "VITO";
                testIntentModel = "gpt-4";

                request = Map.of(
                    "transcribe", testBaseIntent,
                    "intent", testIntent,
                    "user_id", userId.toString(),
                    "stt_model", testSttModel,
                    "intent_model", testIntentModel
                );

                doReturn(false).when(userService).exists(userId);
            }

            @Test
            @DisplayName("Then - Bad Request를 반환한다")
            public void thenShouldReturnBadRequest() {
                var response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/papi/v1/voice-command")
                    .then()
                    .status(HttpStatus.BAD_REQUEST)
                    .apply(
                        document(
                            getNestedClassPath(this.getClass())+"/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            responseErrorFields(UserErrorCode.USER_NOT_FOUND)
                        )
                    );

                assertErrorResponse(response, UserErrorCode.USER_NOT_FOUND);
            }
        }

        @Nested
        @DisplayName("When - 필수 필드가 누락된 요청을 할 때")
        class WhenRequestWithMissingRequiredFields {

            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                request = Map.of(
                    "base_intent", "testBaseIntent",
                    "user_id", generateUserId().toString()
                );
            }

            @Test
            @DisplayName("Then - Bad Request를 반환한다")
            public void thenShouldReturnBadRequest() {
                given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/papi/v1/voice-command")
                    .then()
                    .status(HttpStatus.BAD_REQUEST)
                    .apply(
                        document(
                            getNestedClassPath(this.getClass())+"/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            responseErrorFields(GlobalErrorCode.FIELD_REQUIRED)
                        )
                    );
            }
        }

        @Nested
        @DisplayName("When - 지원하지 않는 STT 모델로 요청할 때")
        class WhenRequestWithUnsupportedSttModel {

            private String testBaseIntent;
            private String testIntent;
            private UUID userId;
            private String invalidSttModel;
            private String testIntentModel;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                testBaseIntent = "testBaseIntent";
                testIntent = "testIntent";
                userId = generateUserId();
                invalidSttModel = "INVALID_STT";
                testIntentModel = "gpt-4";

                request = Map.of(
                    "transcribe", testBaseIntent,
                    "intent", testIntent,
                    "user_id", userId.toString(),
                    "stt_model", invalidSttModel,
                    "intent_model", testIntentModel
                );

                when(userService.exists(userId)).thenReturn(true);
                doThrow(new VoiceCommandHistoryException(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL))
                    .when(voiceCommandHistoryservice)
                    .create(anyString(), anyString(), any(UUID.class), anyString(), anyString());
            }

            @Test
            @DisplayName("Then - Bad Request를 반환한다")
            public void thenShouldReturnBadRequest() {
                var response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/papi/v1/voice-command")
                    .then()
                    .status(HttpStatus.BAD_REQUEST)
                    .apply(
                        document(
                            getNestedClassPath(this.getClass())+"/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            responseErrorFields(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL)
                        )
                    );

                assertErrorResponse(response, VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL);
            }
        }

        @Nested
        @DisplayName("When - 지원하지 않는 Intent 모델로 요청할 때")
        class WhenRequestWithUnsupportedIntentModel {

            private String testBaseIntent;
            private String testIntent;
            private UUID userId;
            private String testSttModel;
            private String invalidIntentModel;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                testBaseIntent = "testBaseIntent";
                testIntent = "testIntent";
                userId = generateUserId();
                testSttModel = "VITO";
                invalidIntentModel = "INVALID_INTENT";

                request = Map.of(
                    "transcribe", testBaseIntent,
                    "intent", testIntent,
                    "user_id", userId.toString(),
                    "stt_model", testSttModel,
                    "intent_model", invalidIntentModel
                );

                when(userService.exists(userId)).thenReturn(true);
                doThrow(new VoiceCommandHistoryException(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL))
                    .when(voiceCommandHistoryservice)
                    .create(anyString(), anyString(), any(UUID.class), anyString(), anyString());
            }

            @Test
            @DisplayName("Then - Bad Request를 반환한다")
            public void thenShouldReturnBadRequest() {
                var response = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/papi/v1/voice-command")
                    .then()
                    .status(HttpStatus.BAD_REQUEST)
                    .apply(
                        document(
                            getNestedClassPath(this.getClass())+"/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            responseErrorFields(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL)
                        )
                    );

                assertErrorResponse(response, VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL);
            }
        }
    }

    private UUID generateUserId() {
        return UUID.randomUUID();
    }
}