package com.personal.interview.domain.auth.entity;

import com.personal.interview.domain.base.LongTypeIdentifier;
import com.personal.interview.domain.base.LongTypeIdentifierJavaType;

public class EmailVerifyId extends LongTypeIdentifier {
	public static EmailVerifyId of(Long id) {
		return new EmailVerifyId(id);
	}

	public EmailVerifyId(Long id) {
		super(id);
	}

	public static class EmailVerifyIdJavaType extends LongTypeIdentifierJavaType<EmailVerifyId> {
		public EmailVerifyIdJavaType() {
			super(EmailVerifyId.class);
		}
	}
}
