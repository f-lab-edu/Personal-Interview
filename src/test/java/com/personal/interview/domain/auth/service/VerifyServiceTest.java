package com.personal.interview.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.auth.AuthPropertiesFixture;
import com.personal.interview.domain.auth.entity.EmailVerify;
import com.personal.interview.domain.auth.repository.EmailVerifyRepository;
import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.global.config.properties.AuthProperties;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.domain.user.entity.vo.UserRole;
import com.personal.interview.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VerifyServiceTest {

	@Autowired
	VerifyService verifyService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailVerifyRepository emailVerifyRepository;

	@Autowired
	EntityManager em;

	@MockitoBean
	ApplicationEventPublisher eventPublisher;

	@MockitoBean
	EmailVerifySender emailVerifySender;

	private PasswordEncoder passwordEncoder;
	private AuthProperties authProperties;

	@BeforeEach
	void setUp() {
		passwordEncoder = mock(PasswordEncoder.class);
		when(passwordEncoder.encode(anyString()))
			.thenAnswer(invocation -> "$2a$10$encoded_" + invocation.getArgument(0));
		
		authProperties = AuthPropertiesFixture.createDefault();
	}

	@Test
	@DisplayName("인증 이메일 발송 성공 - 신규 생성")
	void sendVerifyEmail_firstCreateSuccess() {
		// given
		User user = userRepository.save(UserFixture.createDefaultUser(passwordEncoder));
		UserId userId = user.getId();
		em.flush();
		em.clear();

		// when
		EmailVerify result = verifyService.sendVerifyEmail(userId);
		em.flush();
		em.clear();

		// then
		Optional<EmailVerify> savedVerify = emailVerifyRepository.findTopWithLockByUserIdOrderByIdDesc(userId);
		assertTrue(savedVerify.isPresent());
		assertThat(savedVerify.get().getVerificationToken()).isEqualTo(result.getVerificationToken());
		assertThat(savedVerify.get().getSendCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("인증 이메일 발송 성공 - 재발송(reCreate)")
	void sendVerifyEmail_reCreateSuccess() {
		// given
		User user = userRepository.save(UserFixture.createDefaultUser(passwordEncoder));
		UserId userId = user.getId();
		verifyService.sendVerifyEmail(userId);
		em.flush();
		em.clear();

		// when
		EmailVerify result = verifyService.sendVerifyEmail(userId);
		em.flush();
		em.clear();

		// then
		EmailVerify savedVerify = emailVerifyRepository.findTopWithLockByUserIdOrderByIdDesc(userId).get();
		assertThat(savedVerify.getSendCount()).isEqualTo(2);
	}

	@Test
	@DisplayName("존재하지 않는 사용자일 경우 예외가 발생한다")
	void sendVerifyEmail_userNotFoundFail() {
		// given
		UserId nonExistId = new UserId(999L);

		// when & then
		assertThatThrownBy(() -> verifyService.sendVerifyEmail(nonExistId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("존재하지 않는 사용자입니다.");
	}


	@Test
	@DisplayName("유효한 토큰으로 인증 시, 이메일 인증 상태가 변경되고 사용자의 권한이 승격된다")
	void verifyEmail_Success() {
		// given
		User user = userRepository.save(UserFixture.createDefaultUser(passwordEncoder));
		EmailVerify emailVerify = emailVerifyRepository.save(EmailVerify.create(user.getId(), authProperties));
		UUID token = emailVerify.getVerificationToken();
		assertThat(user.getRole()).isEqualTo(UserRole.ROLE_DRAFT);


		em.flush();
		em.clear();

		// when
		verifyService.verifyEmail(token);
		em.flush();
		em.clear();

		// then
		EmailVerify resultVerify = emailVerifyRepository.findByUserIdAndVerificationToken(user.getId(), token)
			.orElseThrow();
		User resultUser = userRepository.findById(user.getId()).orElseThrow();

		assertTrue(resultVerify.isVerify());
		assertThat(resultUser.getRole()).isEqualTo(UserRole.ROLE_USER);
	}

	@Test
	@DisplayName("토큰이 일치하지 않으면 예외가 발생한다")
	void verifyEmail_InvalidTokenFail() {
		// given
		User user = userRepository.save(UserFixture.createDefaultUser(passwordEncoder));
		emailVerifyRepository.save(EmailVerify.create(user.getId(), authProperties));
		UUID invalidToken = UUID.randomUUID();

		em.flush();
		em.clear();

		// when & then
		assertThatThrownBy(() -> verifyService.verifyEmail(invalidToken))
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
	}

	@Test
	@DisplayName("사용자 ID가 존재하지 않으면 예외가 발생한다")
	void verifyEmail_UserNotFoundFail() {
		// given
		EmailVerify emailVerify = emailVerifyRepository.save(EmailVerify.create(new UserId(999L), authProperties));

		// when & then
		assertThatThrownBy(() -> verifyService.verifyEmail(emailVerify.getVerificationToken()))
			.isInstanceOf(IllegalArgumentException.class);
	}
}