package com.max.test.exception;

import org.springframework.validation.ObjectError;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<ObjectError> errors;

    public ValidationException(String message, List<ObjectError> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(List<ObjectError> errors) {
        this.errors = errors;
    }

    public List<ObjectError> getErrors() {
        return errors;
    }
}
