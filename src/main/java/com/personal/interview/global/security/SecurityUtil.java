package com.personal.interview.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.personal.interview.domain.user.entity.UserId;

public class SecurityUtil {
	public static UserId getCurrentUserId() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getName() == null) {
			throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
		}

		return new UserId(Long.parseLong(authentication.getName()));
	}
}