package com.personal.interview.global.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
	private final BaseErrorCode errorCode;

	private DomainException(BaseErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public static DomainException create(BaseErrorCode errorCode) {
		return new DomainException(errorCode);
	}
}
