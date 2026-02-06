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

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR));
    }

    @ExceptionHandler(CheftoryException.class)
    public ResponseEntity<ErrorResponse> handleCheftoryException(CheftoryException ex) {
        if (ex instanceof AuthException && ex.getError() == AuthErrorCode.INVALID_TOKEN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(ex));
        } else {
            log.error(ex.getError().getMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        String codeName =
                Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        Error error = resolveErrorCode(codeName);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.FIELD_REQUIRED));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.MISSING_HEADER));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR));
    }
}
