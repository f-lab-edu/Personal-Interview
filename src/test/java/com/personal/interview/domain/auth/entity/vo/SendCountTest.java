package com.personal.interview.domain.auth.entity.vo;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SendCountTest {
	@Test
	@DisplayName("0에서 5 사이의 유효한 값으로 생성에 성공한다")
	void create_Success() {
		assertThatCode(() -> new SendCount(0)).doesNotThrowAnyException();
		assertThatCode(() -> new SendCount(3)).doesNotThrowAnyException();
		assertThatCode(() -> new SendCount(5)).doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 6})
	@DisplayName("범위를 벗어난 값(음수 또는 5 초과)은 예외가 발생한다")
	void create_Fail(int value) {
		assertThatThrownBy(() -> new SendCount(value))
			.isInstanceOf(EmailVerifySendException.class);
	}

	@Test
	@DisplayName("최대치인 5에서 값을 증가시키면 예외가 발생한다")
	void increment_Fail_Limit() {
		SendCount sendCount = new SendCount(5);

		assertThatThrownBy(sendCount::increment)
			.isInstanceOf(EmailVerifySendException.class);
	}

}