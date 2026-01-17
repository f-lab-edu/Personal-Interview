package com.personal.interview.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.domain.user.entity.Email;
import com.personal.interview.domain.user.entity.JobCategory;
import com.personal.interview.domain.user.entity.SignUpRequest;
import com.personal.interview.domain.user.entity.SignUpResponse;
import com.personal.interview.domain.user.entity.User;
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
		Optional<User> byId = userRepository.findById(response.userId());
		assertTrue(byId.isPresent());

		User user = byId.get();

		assertThat(user.getEmail()).isEqualTo(null);

		assertThat(request.jobCategoryNames())
			.containsSubsequence(user.getJobCategories().stream()
				.map(JobCategory::getName)
				.toList());
	}


}