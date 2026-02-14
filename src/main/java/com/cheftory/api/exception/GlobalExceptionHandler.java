package com.cheftory.api.exception;

import com.cheftory.api._common.reponse.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 전역 예외 처리기.
 *
 * <p>모든 예외를 잡아서 적절한 ErrorResponse로 변환하여 반환합니다.</p>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private final ErrorStatusResolver errorStatusResolver = new ErrorStatusResolver();
    private final ValidationErrorMapper validationErrorMapper = new ValidationErrorMapper();

    /**
     * IllegalArgumentException을 처리합니다.
     *
     * @param ex IllegalArgumentException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR));
    }

    /**
     * CheftoryException을 처리합니다.
     *
     * @param ex CheftoryException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(CheftoryException.class)
    public ResponseEntity<ErrorResponse> handleCheftoryException(CheftoryException ex) {
        HttpStatus status = errorStatusResolver.resolve(ex.getError());
        if (ex.getError().getType() == ErrorType.UNAUTHORIZED || ex.getError().getType() == ErrorType.FORBIDDEN) {
            log.warn(ex.getError().getMessage());
        } else {
            log.error(ex.getError().getMessage(), ex);
        }
        return ResponseEntity.status(status).body(ErrorResponse.from(ex));
    }

    /**
     * MethodArgumentNotValidException을 처리합니다.
     *
     * @param ex MethodArgumentNotValidException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        Error error = validationErrorMapper.toError(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(error));
    }

    /**
     * ConstraintViolationException을 처리합니다.
     *
     * @param ex ConstraintViolationException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.FIELD_REQUIRED));
    }

    /**
     * MissingRequestHeaderException을 처리합니다.
     *
     * @param ex MissingRequestHeaderException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.MISSING_HEADER));
    }

    /**
     * MissingServletRequestParameterException을 처리합니다.
     *
     * @param ex MissingServletRequestParameterException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.FIELD_REQUIRED));
    }

    /**
     * HandlerMethodValidationException을 처리합니다.
     *
     * @param ex HandlerMethodValidationException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        log.warn(ex.getMessage());
        Error error = validationErrorMapper.toError(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(error));
    }

    /**
     * HttpMessageNotReadableException을 처리합니다.
     *
     * @param ex HttpMessageNotReadableException 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.FIELD_REQUIRED));
    }

    /**
     * 일반 Exception을 처리합니다.
     *
     * @param ex Exception 인스턴스
     * @return BAD_REQUEST 상태와 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR));
    }
}
