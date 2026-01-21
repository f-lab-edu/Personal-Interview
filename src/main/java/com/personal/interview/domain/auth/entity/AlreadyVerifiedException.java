package com.personal.interview.domain.auth.entity;

import org.springframework.http.HttpStatus;

import com.personal.interview.domain.base.DomainException;

public class AlreadyVerifiedException extends DomainException {
	public AlreadyVerifiedException() {
		super(HttpStatus.BAD_REQUEST, "AUTH", "이미 인증된 사용자이므로 재전송을 요청할 수 없습니다.");
	}
}
