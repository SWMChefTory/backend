package com.cheftory.api.exception;

import static com.cheftory.api.exception.Error.resolveErrorCode;

import com.cheftory.api._common.reponse.ErrorResponse;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전역 예외 처리기.
 *
 * <p>모든 예외를 잡아서 적절한 ErrorResponse로 변환하여 반환합니다.</p>
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
        if (ex instanceof AuthException && ex.getError() == AuthErrorCode.INVALID_TOKEN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(ex));
        } else {
            log.error(ex.getError().getMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(ex));
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
        String codeName =
                Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        Error error = resolveErrorCode(codeName);
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.MISSING_HEADER));
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR));
    }
}
