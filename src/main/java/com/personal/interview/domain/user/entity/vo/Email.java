package com.personal.interview.domain.user.entity.vo;

import java.util.regex.Pattern;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(@NaturalId @Column(name = "email", length = 255, unique = true) String value) {
	private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	public Email {
		if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("Invalid email format: " + value);
		}
	}
}
