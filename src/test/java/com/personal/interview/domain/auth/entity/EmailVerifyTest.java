package com.personal.interview.domain.auth.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.personal.interview.domain.auth.entity.vo.SendCount;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;

class EmailVerifyTest {

	@Test
	void create_Success() {
		// given
		UserId userId = new UserId(1L);
		LocalDateTime now = LocalDateTime.now();

		// when
		EmailVerify result = EmailVerify.create(userId);

		// then
		assertAll(
			() -> assertThat(result.getUserId()).isEqualTo(userId),
			() -> assertThat(result.getVerificationToken()).isNotNull(),
			() -> assertThat(result.isVerify()).isFalse(),
			() -> assertThat(result.getSendCount().value()).isEqualTo(1),

			() -> assertThat(result.getFirstAt()).isAfterOrEqualTo(now),
			() -> assertThat(result.getExpiredAt()).isEqualToIgnoringNanos(result.getFirstAt().plusMinutes(10)),
			() -> assertThat(result.getCountResetAt()).isEqualToIgnoringNanos(result.getFirstAt().plusHours(24))
		);
	}

	@Test
	@DisplayName("재발송 시 발송 횟수가 1 증가하고 만료 시간이 갱신된다")
	void reCreate_Success() {
		// given: 초기 발송 상태 (count=1)
		EmailVerify original = EmailVerify.create(new UserId(1L));
		LocalDateTime previousExpiry = original.getExpiredAt();

		// when
		EmailVerify result = original.reCreate();

		// then
		assertAll(
			() -> assertThat(result.getSendCount().value()).isEqualTo(2),
			() -> assertThat(result.getExpiredAt()).isAfterOrEqualTo(previousExpiry),
			() -> assertThat(result.getVerificationToken()).isEqualTo(original.getVerificationToken()),
			() -> assertThat(result.getUserId()).isEqualTo(original.getUserId())
		);
	}

	@Test
	@DisplayName("이미 인증된 상태에서 재발송을 시도하면 예외가 발생한다")
	void reCreate_Fail_AlreadyVerified() {
		// given
		EmailVerify original = EmailVerify.create(new UserId(1L));
		ReflectionTestUtils.setField(original, "isVerify", true);

		// when & then
		assertThatThrownBy(original::reCreate)
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.ALREADY_VERIFIED));
	}

	@Test
	@DisplayName("최대 발송 횟수(5회)를 초과하여 재발송을 시도하면 예외가 발생한다")
	void reCreate_Fail_ExceedLimit() {
		// given: 발송 횟수를 5로 설정
		EmailVerify original = EmailVerify.create(new UserId(1L));
		ReflectionTestUtils.setField(original, "sendCount", new SendCount(5));

		// when & then
		assertThatThrownBy(original::reCreate)
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.EXCEED_MAX_SEND_COUNT));
	}

	@Test
	@DisplayName("카운트 리셋 시간이 지났다면 발송 횟수가 1로 초기화된 새 객체를 반환한다")
	void reCreate_ResetCount_WhenTimePassed() {
		// given
		EmailVerify original = EmailVerify.create(new UserId(1L));
		ReflectionTestUtils.setField(original, "countResetAt", LocalDateTime.now().minusSeconds(1));
		ReflectionTestUtils.setField(original, "sendCount", new SendCount(3));

		// when
		EmailVerify result = original.reCreate();

		// then
		assertThat(result.getSendCount().value()).isEqualTo(1);
		assertThat(result.getVerificationToken()).isNotEqualTo(original.getVerificationToken());
	}

	@Test
	@DisplayName("만료되지 않았고 인증되지 않은 상태에서 verify 호출 시 성공한다")
	void verify_Success() {
		// given
		EmailVerify emailVerify = EmailVerify.create(new UserId(1L));

		// when
		emailVerify.verify();

		// then
		assertThat(emailVerify.isVerify()).isTrue();
	}

	@Test
	@DisplayName("이미 인증된 상태에서 verify 호출 시 DomainException(ALREADY_VERIFIED)이 발생한다")
	void verify_Fail_AlreadyVerified() {
		// given
		EmailVerify emailVerify = EmailVerify.create(new UserId(1L));
		ReflectionTestUtils.setField(emailVerify, "isVerify", true);

		// when & then
		assertThatThrownBy(emailVerify::verify)
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.ALREADY_VERIFIED));
	}

	@Test
	@DisplayName("인증 만료 시간이 지난 후 verify 호출 시 DomainException(VERIFICATION_EXPIRED)이 발생한다")
	void verify_Fail_Expired() {
		// given
		EmailVerify emailVerify = EmailVerify.create(new UserId(1L));
		// 만료 시간을 1초 전으로 설정
		ReflectionTestUtils.setField(emailVerify, "expiredAt", LocalDateTime.now().minusSeconds(1));

		// when & then
		assertThatThrownBy(emailVerify::verify)
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.VERIFICATION_EXPIRED));
	}

}