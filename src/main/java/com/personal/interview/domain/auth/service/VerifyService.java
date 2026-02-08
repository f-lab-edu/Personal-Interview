package com.personal.interview.domain.auth.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.auth.entity.EmailVerify;
import com.personal.interview.domain.auth.entity.EmailVerifySendEvent;
import com.personal.interview.domain.auth.repository.EmailVerifyRepository;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.domain.user.repository.UserRepository;

import com.personal.interview.global.config.properties.AuthProperties;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyService {
	private final EmailVerifyRepository emailVerifyRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final AuthProperties authProperties;

	@Transactional
	public EmailVerify sendVerifyEmail(UserId userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		EmailVerify result = emailVerifyRepository.findTopWithLockByUserIdOrderByIdDesc(userId)
			.map(ev -> ev.reCreate(authProperties))
			.orElseGet(() -> EmailVerify.create(userId, authProperties));

		eventPublisher.publishEvent(new EmailVerifySendEvent(user.getEmail(), result.getVerificationToken()));

		return emailVerifyRepository.save(result);
	}

	@Transactional
	public EmailVerify verifyEmail(UUID token) {
		EmailVerify emailVerify = emailVerifyRepository.findTopByVerificationTokenOrderByIdDesc(token)
			.orElseThrow(() -> DomainException.create(ErrorCode.INVALID_TOKEN));

		User user = userRepository.findById(emailVerify.getUserId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		emailVerify.verify();

		user.modifyRoleUser();

		userRepository.save(user);

		return emailVerifyRepository.save(emailVerify);
	}
}
