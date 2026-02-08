package com.personal.interview.domain.user.entity;

import com.personal.interview.domain.base.LongTypeIdentifier;
import com.personal.interview.domain.base.LongTypeIdentifierJavaType;

public class UserId extends LongTypeIdentifier {
	public static UserId of(Long id) {
		return new UserId(id);
	}

	public UserId(Long id) {
		super(id);
	}

	public static class UserIdJavaType extends LongTypeIdentifierJavaType<UserId> {
		public UserIdJavaType() {
			super(UserId.class);
		}
	}
}
