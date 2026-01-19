package com.personal.interview.domain.user.service;

import static com.personal.interview.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.domain.user.entity.JobCategory;
import com.personal.interview.domain.user.entity.dto.SignUpRequest;
import com.personal.interview.domain.user.entity.dto.SignUpResponse;
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
			.isInstanceOf(IllegalArgumentException.class);
	}

}