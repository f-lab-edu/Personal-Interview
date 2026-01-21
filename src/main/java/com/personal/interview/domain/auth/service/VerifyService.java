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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyService {
	private final EmailVerifyRepository emailVerifyRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public EmailVerify sendVerifyEmail(UserId userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		EmailVerify result = emailVerifyRepository.findTopByUserIdOrderByIdDesc(userId)
			.map(EmailVerify::reCreate)
			.orElseGet(() -> EmailVerify.create(userId));

		eventPublisher.publishEvent(new EmailVerifySendEvent(user.getEmail(), result.getVerificationToken()));

		return emailVerifyRepository.save(result);
	}

	@Transactional
	public EmailVerify verifyEmail(UserId userId, UUID token) {
		EmailVerify emailVerify = emailVerifyRepository.findByUserIdAndVerificationToken(userId, token)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		emailVerify.verify();

		user.modifyRoleUser();

		userRepository.save(user);

		return emailVerifyRepository.save(emailVerify);
	}
}
