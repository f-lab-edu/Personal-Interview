package com.personal.interview.domain.user.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.personal.interview.domain.user.entity.vo.Email;

class EmailTest {

	@Test
	void newEmailTest(){
		String value = "email@testEmail";

		Email email = new Email(value);
	}

	@Test
	void newTestFail(){
		String wrongValue = "wrongEmailFormat";

		assertThatThrownBy(() ->
			new Email(wrongValue)
		).isInstanceOf(IllegalArgumentException.class);


		String nullValue = null;

		assertThatThrownBy(() ->
			new Email(nullValue)
		).isInstanceOf(IllegalArgumentException.class);
	}

}