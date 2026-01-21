package com.personal.interview.domain.auth.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record SendCount(@Column(name = "send_count", nullable = false) int value) {
	public SendCount {
		if (value < 0 || value > 5) {
			throw new EmailVerifySendException();
		}
	}

	public SendCount increment() {
		return new SendCount(this.value + 1);
	}
}
