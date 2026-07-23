package com.shop.productservice.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFound(ProductNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleEnumErrors(MethodArgumentTypeMismatchException ex) {

        // Check if the field is an enum
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {

            Object rawValue = ex.getValue();
            String invalidValue = rawValue != null ? rawValue.toString() : "null";
            String fieldName = ex.getName();

            // Allowed enum values — safe since isEnum() confirmed above
            Object[] constants = requiredType.getEnumConstants();
            String allowedValues = constants != null ? Arrays.toString(constants) : "N/A";

            String message = "Invalid value '" + invalidValue + "' for " + fieldName
                    + ". Allowed values: " + allowedValues;

            return ResponseEntity.badRequest().body(message);
        }

        // Other mismatches → return generic
        return ResponseEntity.badRequest().body("Invalid request parameter.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getCause();

        // Case 1: ENUM deserialization error
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormatException) {

            Class<?> targetType = invalidFormatException.getTargetType();

            if (targetType.isEnum()) {
                String invalidValue = invalidFormatException.getValue().toString();
                String fieldName = invalidFormatException.getPath().get(0).getFieldName();
                String allowedValues = Arrays.toString(targetType.getEnumConstants());

                String message = "Invalid value '" + invalidValue + "' for field '" + fieldName +
                        "'. Allowed values: " + allowedValues;

                return ResponseEntity.badRequest().body(message);
            }
        }

        // Case 2: Not an enum issue → generic JSON parse error
        return ResponseEntity.badRequest().body("Invalid request JSON: " + ex.getMostSpecificCause().getMessage());
    }


    @ExceptionHandler(VariantNotFoundException.class)
    public ResponseEntity<String> handleVariantNotFound(VariantNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidProductException.class)
    public ResponseEntity<String> handleInvalidProduct(InvalidProductException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllOtherExceptions(Exception ex) {
        return new ResponseEntity<>("Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);
    }

}
