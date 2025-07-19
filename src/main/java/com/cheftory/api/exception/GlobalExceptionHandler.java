package com.cheftory.api.exception;

import static com.cheftory.api.exception.ErrorMessage.resolveErrorCode;

import com.cheftory.api._common.reponse.ErrorResponse;

import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.user.exception.UserException;
import com.cheftory.api.voicecommand.VoiceCommandHistoryException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse response = ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getErrorCode().getMessage());
        response.put("error", ex.getErrorCode().name());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, String>> handleUserException(UserException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message", e.errorCode.getMessage(),
                        "error", e.errorCode.name()
                ));
    }

    @ExceptionHandler(CheftoryException.class)
    public ResponseEntity<ErrorResponse> handleCheftoryException(CheftoryException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(GlobalErrorCode.UNKNOWN_ERROR));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String codeName = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();

        ErrorMessage errorMessage = resolveErrorCode(codeName);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(GlobalErrorCode.FIELD_REQUIRED));
    }
}
