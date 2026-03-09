package com.personal.interview.util.validator;

import org.springframework.security.core.AuthenticationException;

public class SecurityFilterValidationException extends AuthenticationException {
    public SecurityFilterValidationException(String msg) {
        super(msg);
    }
}
