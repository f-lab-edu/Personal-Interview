package com.personal.interview.domain.auth.controller.dto;

import java.time.LocalDateTime;

import com.personal.interview.domain.auth.entity.EmailVerify;

public record EmailVerifyResponse(
	LocalDateTime expiredAt,
	int sendCount,
	boolean isVerify,
	LocalDateTime countResetAt
) {
	public static EmailVerifyResponse from(EmailVerify emailVerify) {
		return new EmailVerifyResponse(
			emailVerify.getExpiredAt(),
			emailVerify.getSendCount().value(),
			emailVerify.isVerify(),
			emailVerify.getCountResetAt()
		);
	}
}
