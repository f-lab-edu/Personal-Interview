package com.personal.interview.domain.user.entity;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(@Column(name = "email", length = 255, nullable = true) String value) {
	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	public Email {
		if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("Invalid email format: " + value);
		}
	}
}
