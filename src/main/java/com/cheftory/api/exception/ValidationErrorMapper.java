package com.cheftory.api.exception;

import static com.cheftory.api.exception.Error.resolveErrorCode;

import java.util.Objects;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 검증 예외를 도메인 Error로 매핑하는 컴포넌트.
 */
@Component
public class ValidationErrorMapper {

    /**
     * MethodArgumentNotValidException을 Error로 매핑합니다.
     *
     * @param ex 예외
     * @return 매핑된 Error
     */
    public Error toError(MethodArgumentNotValidException ex) {
        String codeName =
                Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return resolveValidationError(codeName);
    }

    /**
     * HandlerMethodValidationException을 Error로 매핑합니다.
     *
     * @param ex 예외
     * @return 매핑된 Error
     */
    public Error toError(HandlerMethodValidationException ex) {
        String codeName = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(GlobalErrorCode.FIELD_REQUIRED.getErrorCode());
        return resolveValidationError(codeName);
    }

    private Error resolveValidationError(String codeName) {
        Error error = resolveErrorCode(codeName);
        return error == GlobalErrorCode.UNKNOWN_ERROR ? GlobalErrorCode.FIELD_REQUIRED : error;
    }
}
