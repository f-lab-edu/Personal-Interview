package com.personal.interview.domain.auth.entity;

import static com.personal.interview.domain.auth.entity.EmailVerifyId.*;
import static jakarta.persistence.GenerationType.*;
import static java.util.Objects.*;
import static java.util.UUID.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JavaType;

import com.personal.interview.domain.auth.entity.vo.SendCount;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_verify")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EmailVerify{
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@JavaType(EmailVerifyIdJavaType.class)
	private EmailVerifyId id;

	@Column(nullable = false)
	private UUID verificationToken;

	@Column(nullable = false)
	private boolean isVerify;

	@Embedded
	private SendCount sendCount;

	@Column(name = "user_id", nullable = false)
	private UserId userId;

	@Column(nullable = false)
	private LocalDateTime firstAt;

	@Column(nullable = false)
	private LocalDateTime expiredAt;

	@Column(nullable = false)
	private LocalDateTime countResetAt;

	private static final int MIN_SEND_COUNT = 1;
	private static final int MAX_SEND_COUNT = 5;
	private static final int EXPIRATION_MINUTES = 10;
	private static final int COUNT_RESET_HOURS = 24;

	public static EmailVerify create(UserId userId) {
		EmailVerify emailVerify = new EmailVerify();

		emailVerify.verificationToken = randomUUID(); //TODO : 시간 정렬 가능한 UUID로 변경 고려
		emailVerify.isVerify = false;
		emailVerify.sendCount = new SendCount(MIN_SEND_COUNT);
		emailVerify.userId = requireNonNull(userId);
		emailVerify.firstAt = LocalDateTime.now();
		emailVerify.expiredAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
		emailVerify.countResetAt = LocalDateTime.now().plusHours(COUNT_RESET_HOURS);

		return emailVerify;
	}

	public EmailVerify reCreate() {
		EmailVerify emailVerify = new EmailVerify();

		LocalDateTime now = LocalDateTime.now();

		if (now.isAfter(this.countResetAt)) {
			return create(this.userId);
		}
		if (this.isVerify) {
			throw DomainException.create(ErrorCode.ALREADY_VERIFIED);
		}
		if(this.sendCount.value() >= MAX_SEND_COUNT) {
			throw DomainException.create(ErrorCode.EXCEED_MAX_SEND_COUNT);
		}

		emailVerify.verificationToken = this.verificationToken;
		emailVerify.isVerify = false;
		emailVerify.sendCount = this.sendCount.increment();
		emailVerify.userId = this.userId;
		emailVerify.firstAt = this.firstAt;
		emailVerify.expiredAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
		emailVerify.countResetAt = this.countResetAt;

		return emailVerify;
	}

	public void verify() {
		if (this.isVerify) {
			throw DomainException.create(ErrorCode.ALREADY_VERIFIED);
		}
		if (LocalDateTime.now().isAfter(this.expiredAt)) {
			throw DomainException.create(ErrorCode.VERIFICATION_EXPIRED);
		}

		this.isVerify = true;
	}
}
