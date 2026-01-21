package com.personal.interview.domain.auth.entity.vo;

import com.personal.interview.domain.base.VoException;

public class EmailVerifySendException extends VoException {
	public EmailVerifySendException() {
		super("AUTH", "이메일 인증 횟수에 오류가 발생했습니다.");
	}
}
