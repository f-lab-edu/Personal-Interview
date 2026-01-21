package com.personal.interview.domain.base;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
	protected HttpStatus status;
	protected String code;
	protected String message;

	public DomainException(HttpStatus status, String code, String message) {
		super(message);

		this.status = status;
		this.code = code;
		this.message = message;
	}
}
