package com.tripsi.api.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

// Central RFC 9457 error handling; never leaks stack traces or internal class names in responses.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Returns 404 when a requested resource is not found.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Returns 422 when a domain/business rule is violated.
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violated: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // Returns 401 on bad credentials; logged as a security event per CLAUDE.md.
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        log.info("Authentication failed: invalid credentials");
        return buildProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    // Returns 403 on access denied; logged as a security event per CLAUDE.md.
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.info("Access denied");
        return buildProblemDetail(HttpStatus.FORBIDDEN, "Access denied");
    }

    // Returns 400 with field-level validation errors for @Valid request bodies.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::errorMessage, (first, second) -> first));

        ProblemDetail problemDetail = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setProperty("errors", fieldErrors);
        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    // Catch-all: logs the full exception server-side, exposes only a generic message to the client.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private String errorMessage(FieldError fieldError) {
        return fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value";
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
