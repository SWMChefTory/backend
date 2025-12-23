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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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

    mockMvc =
        mockMvcBuilder(controller)
            .withValidator(UserService.class, userService)
            .withAdvice(exceptionHandler)
            .build();
  }

  @Nested
  @DisplayName("음성 명령 기록 생성")
  class CreateVoiceCommandHistory {

    @Nested
    @DisplayName("Given - 유효한 음성 명령 요청이 주어졌을 때")
    class GivenValidVoiceCommandRequest {

      private String testBaseIntent;
      private String testIntent;
      private UUID userId;
      private String testSttModel;
      private String testIntentModel;
      private Integer start;
      private Integer end;
      private Map<String, Object> request;

      @BeforeEach
      void setUp() {
        testBaseIntent = "testBaseIntent";
        testIntent = "testIntent";
        userId = generateUserId();
        testSttModel = "VITO";
        testIntentModel = "gpt-4";
        start = 1;
        end = 2;

        request =
            Map.of(
                "transcribe", testBaseIntent,
                "intent", testIntent,
                "user_id", userId.toString(),
                "stt_model", testSttModel,
                "intent_model", testIntentModel,
                "start", start,
                "end", end);
      }

      @Nested
      @DisplayName("When - 음성 명령을 생성할 때")
      class WhenCreatingVoiceCommand {

        @BeforeEach
        void setUp() {
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

        @Test
        @DisplayName("Then - 음성 명령 기록을 성공적으로 생성한다")
        public void thenShouldCreateVoiceCommandHistory() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .body(request)
                  .post("/papi/v1/voice-command")
                  .then()
                  .status(HttpStatus.OK)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
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
              .create(
                  testBaseIntent, testIntent, userId, testSttModel, testIntentModel, start, end);
          verify(userService).exists(userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 사용자 ID가 주어졌을 때")
    class GivenNonExistentUserId {

      private String testBaseIntent;
      private String testIntent;
      private UUID userId;
      private String testSttModel;
      private String testIntentModel;
      private Integer start;
      private Integer end;
      private Map<String, Object> request;

      @BeforeEach
      void setUp() {
        testBaseIntent = "testBaseIntent";
        testIntent = "testIntent";
        userId = generateUserId();
        testSttModel = "VITO";
        testIntentModel = "gpt-4";
        start = 1;
        end = 2;

        request =
            Map.of(
                "transcribe", testBaseIntent,
                "intent", testIntent,
                "user_id", userId.toString(),
                "stt_model", testSttModel,
                "intent_model", testIntentModel,
                "start", start,
                "end", end);

        doReturn(false).when(userService).exists(userId);
      }

      @Nested
      @DisplayName("When - 음성 명령을 생성할 때")
      class WhenCreatingVoiceCommand {

        @Test
        @DisplayName("Then - Bad Request를 반환한다")
        public void thenShouldReturnBadRequest() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .body(request)
                  .post("/papi/v1/voice-command")
                  .then()
                  .status(HttpStatus.BAD_REQUEST)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          responseErrorFields(UserErrorCode.USER_NOT_FOUND)));

          assertErrorResponse(response, UserErrorCode.USER_NOT_FOUND);
        }
      }
    }

    @Nested
    @DisplayName("Given - 필수 필드가 누락된 요청이 주어졌을 때")
    class GivenMissingRequiredFields {

      private Map<String, Object> request;

      @BeforeEach
      void setUp() {
        request = Map.of("base_intent", "testBaseIntent", "user_id", generateUserId().toString());
      }

      @Nested
      @DisplayName("When - 음성 명령을 생성할 때")
      class WhenCreatingVoiceCommand {

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
                      getNestedClassPath(this.getClass()) + "/{method-name}",
                      requestPreprocessor(),
                      responsePreprocessor(),
                      responseErrorFields(GlobalErrorCode.FIELD_REQUIRED)));
        }
      }
    }

    @Nested
    @DisplayName("Given - 지원하지 않는 STT 모델이 주어졌을 때")
    class GivenUnsupportedSttModel {

      private String testBaseIntent;
      private String testIntent;
      private UUID userId;
      private String invalidSttModel;
      private String testIntentModel;
      private Integer start;
      private Integer end;
      private Map<String, Object> request;

      @BeforeEach
      void setUp() {
        testBaseIntent = "testBaseIntent";
        testIntent = "testIntent";
        userId = generateUserId();
        invalidSttModel = "INVALID_STT";
        testIntentModel = "gpt-4";
        start = 1;
        end = 2;

        request =
            Map.of(
                "transcribe", testBaseIntent,
                "intent", testIntent,
                "user_id", userId.toString(),
                "stt_model", invalidSttModel,
                "intent_model", testIntentModel,
                "start", start,
                "end", end);

        doReturn(true).when(userService).exists(any(UUID.class));
        doThrow(
                new VoiceCommandHistoryException(
                    VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL))
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
      @DisplayName("When - 음성 명령을 생성할 때")
      class WhenCreatingVoiceCommand {

        @Test
        @DisplayName("Then - Bad Request를 반환한다")
        public void thenShouldReturnBadRequest() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .body(request)
                  .post("/papi/v1/voice-command")
                  .then()
                  .status(HttpStatus.BAD_REQUEST)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          responseErrorFields(
                              VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL)));

          assertErrorResponse(response, VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL);
          verify(userService).exists(userId);
          verify(voiceCommandHistoryservice)
              .create(
                  testBaseIntent, testIntent, userId, invalidSttModel, testIntentModel, start, end);
        }
      }
    }

    @Nested
    @DisplayName("Given - 지원하지 않는 Intent 모델이 주어졌을 때")
    class GivenUnsupportedIntentModel {

      private String testBaseIntent;
      private String testIntent;
      private UUID userId;
      private String testSttModel;
      private String invalidIntentModel;
      private Integer start;
      private Integer end;
      private Map<String, Object> request;

      @BeforeEach
      void setUp() {
        testBaseIntent = "testBaseIntent";
        testIntent = "testIntent";
        userId = generateUserId();
        testSttModel = "VITO";
        invalidIntentModel = "INVALID_INTENT";
        start = 1;
        end = 2;

        request =
            Map.of(
                "transcribe", testBaseIntent,
                "intent", testIntent,
                "user_id", userId.toString(),
                "stt_model", testSttModel,
                "intent_model", invalidIntentModel,
                "start", start,
                "end", end);

        doReturn(true).when(userService).exists(any(UUID.class));
        doThrow(
                new VoiceCommandHistoryException(
                    VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL))
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
      @DisplayName("When - 음성 명령을 생성할 때")
      class WhenCreatingVoiceCommand {

        @Test
        @DisplayName("Then - Bad Request를 반환한다")
        public void thenShouldReturnBadRequest() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .body(request)
                  .post("/papi/v1/voice-command")
                  .then()
                  .status(HttpStatus.BAD_REQUEST)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          responseErrorFields(
                              VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL)));

          assertErrorResponse(response, VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL);
          verify(userService).exists(userId);
          verify(voiceCommandHistoryservice)
              .create(
                  testBaseIntent, testIntent, userId, testSttModel, invalidIntentModel, start, end);
        }
      }
    }
  }

  private UUID generateUserId() {
    return UUID.randomUUID();
  }
}
