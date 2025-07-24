package com.cheftory.api.utils;


import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.voicecommand.STTModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public class RestDocsUtils {

    public static OperationRequestPreprocessor requestPreprocessor() {
        return Preprocessors.preprocessRequest(
                Preprocessors.modifyUris().scheme("http").host("test.host").removePort(),
                Preprocessors.prettyPrint()
        );
    }

    public static OperationResponsePreprocessor responsePreprocessor() {
        return Preprocessors.preprocessResponse(Preprocessors.prettyPrint());
    }

    public static ResponseFieldsSnippet responseSuccessFields() {
        return PayloadDocumentation.responseFields(
                PayloadDocumentation.fieldWithPath("message").description("success")
        );
    }

    public static ResponseFieldsSnippet responseErrorFields(ErrorMessage errorMessage) {
        return PayloadDocumentation.responseFields(
                PayloadDocumentation.fieldWithPath("errorCode").description(errorMessage.getErrorCode()),
                PayloadDocumentation.fieldWithPath("message").description(errorMessage.getMessage())
        );
    }

    public static RequestHeadersSnippet requestAccessTokenFields() {
        return HeaderDocumentation.requestHeaders(
                HeaderDocumentation.headerWithName("Authorization").description("Bearer 액세스 토큰")
        );
    }

    public static RequestHeadersSnippet requestRefreshTokenFields() {
        return HeaderDocumentation.requestHeaders(
                HeaderDocumentation.headerWithName("Authorization").description("Bearer 리프레시 토큰")
        );
    }

    /*
     * Enum 클래스의 getValue 메서드를 사용하여 값을 가져오는 방식으로 구현합니다.
     * Enum 클래스가 getValue 메서드를 가지고 있어야 합니다.
     * Enum 클래스의 getValue 메서드가 없을 경우, Enum의 이름을 사용하여 값을 가져옵니다.
     */
    public static FieldDescriptor enumFields(String fieldName, String description,
        Class<? extends Enum<?>> enumClass) {
        String formattedEnumValues = Arrays.stream(enumClass.getEnumConstants())
            .map(enumValue -> {
                try {
                    Method getValueMethod = enumValue.getClass().getMethod("getValue");
                    Object value = getValueMethod.invoke(enumValue);
                    return String.format("`%s`", value.toString());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    return String.format("`%s`", enumValue.name());
                }
            })
            .collect(Collectors.joining(", "));
        return fieldWithPath(fieldName).description(description + " 사용 가능한 값: " + formattedEnumValues);
    }

    public static String getNestedClassPath(Class<?> clazz) {
        StringBuilder path = new StringBuilder();
        while (clazz != null) {
            if (!path.isEmpty()) path.insert(0, "/");
            String className = clazz.getSimpleName()
                .replace("Test", "")
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase();
            path.insert(0, className);
            clazz = clazz.getEnclosingClass();
        }
        return path.toString();
    }
}