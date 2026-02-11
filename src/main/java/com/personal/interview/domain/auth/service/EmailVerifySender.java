package com.personal.interview.domain.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.personal.interview.domain.user.entity.vo.Email;

@Component
public interface EmailVerifySender{
	/**
	 * @param email 수신자 이메일 주소
	 * @param token 인증용 UUID 토큰
	 */
	void sendVerificationEmail(Email email, UUID token);
}
