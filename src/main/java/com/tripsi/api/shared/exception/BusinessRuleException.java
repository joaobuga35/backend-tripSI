package com.tripsi.api.shared.exception;

// Thrown when a domain/business rule is violated; mapped to HTTP 422.
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
