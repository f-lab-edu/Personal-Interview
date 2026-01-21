package com.personal.interview.domain.auth.entity;

import org.springframework.http.HttpStatus;

import com.personal.interview.domain.base.DomainException;

public class ExceedMaxSendCountException extends DomainException {
	public ExceedMaxSendCountException() {
		super(HttpStatus.CONFLICT, "AUTH", "인증 코드 전송 횟수를 초과했습니다.");
	}
}
