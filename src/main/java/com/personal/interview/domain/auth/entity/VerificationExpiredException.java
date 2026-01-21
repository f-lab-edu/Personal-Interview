package com.personal.interview.domain.auth.entity;

import org.springframework.http.HttpStatus;

import com.personal.interview.domain.base.DomainException;

public class VerificationExpiredException extends DomainException {
	public VerificationExpiredException() {
		super(HttpStatus.BAD_REQUEST, "VERIFICATION_EXPIRED", "인증이 만료되었습니다. 다시 시도해주세요.");
	}
}
