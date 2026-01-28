package com.personal.interview.domain.base;

import lombok.Getter;

@Getter
public class VoException extends RuntimeException {
	protected String code;
	protected String description;

	public VoException(String code, String description) {
		super(description);
		this.code = code;
		this.description = description;
	}
}
