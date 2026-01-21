package com.personal.interview.domain.user.entity;

import org.springframework.http.HttpStatus;

import com.personal.interview.domain.base.DomainException;

public class DuplicateEmailException extends DomainException {

    public DuplicateEmailException() {
        super(HttpStatus.BAD_REQUEST, "USER", "Email already exists.");
    }
}
