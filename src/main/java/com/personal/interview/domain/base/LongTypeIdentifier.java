package com.personal.interview.domain.base;

import java.io.Serializable;

public abstract class LongTypeIdentifier extends ValueObject<LongTypeIdentifier> implements Serializable {
	private Long id;

	public LongTypeIdentifier(Long id) {
		this.id = id;
	}

	public Long longValue() {
		return id;
	}

	public Long nextValue() {
		return id + 1;
	}

	@Override
	protected Object[] getEqualityFields() {
		return new Object[] { id };
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " : " + longValue();
	}
}

