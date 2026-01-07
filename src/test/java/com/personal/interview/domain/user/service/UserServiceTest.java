package com.personal.interview.domain.user.service;

import com.personal.interview.domain.user.entity.SignUpRequest;
import com.personal.interview.domain.user.entity.SignUpResponse;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.UserRole;
import com.personal.interview.domain.user.repository.UserRepository;
import com.personal.interview.global.exception.DuplicateEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest(
                "test@example.com",
                "password123",
                "테스터",
                List.of("백엔드", "DevOps"));

        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "$2a$10$encoded_" + invocation.getArgument(0));
    }

    @Test
    @DisplayName("signUp: 정상적인 회원가입 성공")
    void signUp_Success() {
        // given
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // ID를 설정하여 저장된 User처럼 만듦
            return user;
        });

        // when
        SignUpResponse response = userService.signUp(signUpRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getRole()).isEqualTo(UserRole.ROLE_DRAFT);
        assertThat(response.getJobCategoryNames()).hasSize(2);
        assertThat(response.getJobCategoryNames()).containsExactlyInAnyOrder("백엔드", "DevOps");

        // verify
        verify(userRepository, times(1)).existsByEmail(signUpRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("signUp: 중복된 이메일로 회원가입 시 DuplicateEmailException 발생")
    void signUp_DuplicateEmail_ThrowsException() {
        // given
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signUp(signUpRequest))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다");

        // verify
        verify(userRepository, times(1)).existsByEmail(signUpRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("signUp: Job Category 없이 회원가입 성공")
    void signUp_WithoutJobCategories_Success() {
        // given
        SignUpRequest requestWithoutJobs = new SignUpRequest(
                "test@example.com",
                "password123",
                "테스터",
                List.of());

        when(userRepository.existsByEmail(requestWithoutJobs.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SignUpResponse response = userService.signUp(requestWithoutJobs);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getJobCategoryNames()).isEmpty();

        // verify
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("signUp: 초기 role은 ROLE_DRAFT")
    void signUp_InitialRoleIsDraft() {
        // given
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SignUpResponse response = userService.signUp(signUpRequest);

        // then
        assertThat(response.getRole()).isEqualTo(UserRole.ROLE_DRAFT);
    }

    @Test
    @DisplayName("signUp: password가 암호화되어 저장됨")
    void signUp_PasswordEncoded() {
        // given
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userService.signUp(signUpRequest);

        // then
        verify(passwordEncoder, times(1)).encode("password123");
    }
}
