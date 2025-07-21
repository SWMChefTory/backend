package com.cheftory.api.utils;


import com.cheftory.api.exception.ErrorMessage;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
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