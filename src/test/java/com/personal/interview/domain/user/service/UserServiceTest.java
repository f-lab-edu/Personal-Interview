package com.personal.interview.domain.user.service;

import static com.personal.interview.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.auth.service.EmailVerifySender;
import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.domain.user.entity.JobCategory;
import com.personal.interview.domain.user.controller.dto.SignUpRequest;
import com.personal.interview.domain.user.controller.dto.SignUpResponse;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {
	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	EntityManager em;

	@MockitoBean
	EmailVerifySender emailVerifySender;

	@Test
	@DisplayName("동시에 같은 이메일로 가입 요청 시 하나만 성공하고 나머지는 실패해야 한다")
	void signUp_Concurrency() {
		// given
		SignUpRequest request = UserFixture.createSignUpRequest("concurrency@example.com");

		// 첫 번째 가입 (성공)
		userService.signUp(request);
		em.flush();
		em.clear();

		// when & then
		// 같은 이메일로 두 번째 가입 (실패해야 함)
		assertThatThrownBy(() -> userService.signUp(request))
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
	}

	@Test
	void signUpTest() {
		// given
		SignUpRequest request = UserFixture
			.createSignUpRequest();

		// when
		SignUpResponse response = userService.signUp(request);
		em.flush();
		em.clear();

		// then
		Optional<User> byId = userRepository.findById(new UserId(response.userId()));
		assertTrue(byId.isPresent());

		User user = byId.get();

		assertThat(user.getEmail()).isEqualTo(new Email(request.email()));

		assertThat(request.jobCategoryNames())
			.containsSubsequence(user.getJobCategories().stream()
				.map(JobCategory::getName)
				.toList());
	}

	@Test
	void signUpTest_existingEmailFail() {
		// given
		SignUpRequest request = createSignUpRequestWithOldEmail();

		userService.signUp(request);
		em.flush();
		em.clear();

		assertThat(userRepository.existsByEmail(new Email(request.email())))
			.isTrue();

		// when & then
		assertThatThrownBy(() -> userService.signUp(request))
			.isInstanceOf(DomainException.class)
			.satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
	}
}
