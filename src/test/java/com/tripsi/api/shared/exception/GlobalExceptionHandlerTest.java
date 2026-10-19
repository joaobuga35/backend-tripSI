package com.tripsi.api.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void should_return_404_when_resource_not_found() {
        ProblemDetail problemDetail = handler.handleResourceNotFound(new ResourceNotFoundException("User not found"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getDetail()).isEqualTo("User not found");
    }

    @Test
    void should_return_422_when_business_rule_violated() {
        ProblemDetail problemDetail = handler.handleBusinessRule(new BusinessRuleException("Email already registered"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Email already registered");
    }

    @Test
    void should_return_401_and_hide_detail_when_credentials_invalid() {
        ProblemDetail problemDetail = handler.handleBadCredentials(new BadCredentialsException("secret internal detail"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Invalid credentials");
    }

    @Test
    void should_return_403_when_access_denied() {
        ProblemDetail problemDetail = handler.handleAccessDenied(new AccessDeniedException("secret internal detail"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Access denied");
    }

    @Test
    void should_return_500_and_hide_exception_detail_when_unexpected_error_occurs() {
        ProblemDetail problemDetail = handler.handleUnexpected(new RuntimeException("npe at com.tripsi.api.internal.Secret"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred");
    }
}
