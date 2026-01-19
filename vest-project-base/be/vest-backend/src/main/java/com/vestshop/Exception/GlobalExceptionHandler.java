package com.vestshop.Exception;
import com.vestshop.Exception.ApiException;
import com.vestshop.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== từ file 2 ==========
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ========== từ file 1 ==========
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> illegal(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ========== gộp 2 cái Exception chung (không bỏ ý) ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> other(Exception ex) {
        // File 1: "Lỗi hệ thống: " + ex.getMessage()
        // File 2: "Internal server error"
        String msg = "Internal server error" + (ex.getMessage() != null ? " | Lỗi hệ thống: " + ex.getMessage() : "");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(msg));
    }
}
