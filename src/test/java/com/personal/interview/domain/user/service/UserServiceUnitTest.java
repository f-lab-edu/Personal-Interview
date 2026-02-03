package com.personal.interview.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.personal.interview.domain.user.UserFixture;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.domain.user.controller.dto.SignUpRequest;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void signUp_Concurrency_DuplicateEmail() {
        // given
        SignUpRequest request = UserFixture.createDefaultSignUpRequest();

        given(userRepository.existsByEmail(any(Email.class))).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        given(userRepository.save(any(User.class)))
            .willThrow(new DataIntegrityViolationException("Duplicate entry"));

        // when & then
        assertThatThrownBy(() -> userService.signUp(request))
            .isInstanceOf(DomainException.class)
            .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }
}
