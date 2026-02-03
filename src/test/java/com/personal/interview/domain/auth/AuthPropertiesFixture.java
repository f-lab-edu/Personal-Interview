package com.personal.interview.domain.auth;

import com.personal.interview.global.config.properties.AuthProperties;

/**
 * 테스트용 AuthProperties Fixture
 */
public class AuthPropertiesFixture {

	public static AuthProperties createDefault() {
		return new AuthProperties(5, 0, 10, 24);
	}

	public static AuthProperties create(int maxSendCount, int minSendCount, int expirationMinutes, int countResetHours) {
		return new AuthProperties(maxSendCount, minSendCount, expirationMinutes, countResetHours);
	}
}
