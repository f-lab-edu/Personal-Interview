package com.personal.interview.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.personal.interview.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.personal.interview.domain.user.entity.JobCategory;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.UserRole;

class UserTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "$2a$10$encoded_" + invocation.getArgument(0));
    }

    @Test
    @DisplayName("signUp: 정상적인 사용자 생성")
    void signUp_Success() {
        // when
        User user = createDefaultUser(passwordEncoder);

        // then
        assertThat(user.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(user.getPassword()).isEqualTo(DEFAULT_ENCODED_PASSWORD);
        assertThat(user.getNickname()).isEqualTo(DEFAULT_NICKNAME);
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_DRAFT); //초기 role은 ROLE_DRAFT
        assertThat(user.getJobCategories()).hasSize(EXPECTED_JOB_COUNT_TWO);
        assertThat(user.getJobCategories())
                .extracting(JobCategory::getJobCategoryName)
                .containsExactlyInAnyOrder(JOB_BACKEND, JOB_DEVOPS);
    }

    @Test
    @DisplayName("modifyEmail: 이메일 변경 성공")
    void modifyEmail_Success() {
        // given
        User user = createUserWithOldEmail(passwordEncoder);

        // when
        user.modifyEmail(NEW_EMAIL);

        // then
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
    }

    @Test
    @DisplayName("modifyRoleUser: ROLE_DRAFT에서 ROLE_USER로 승급 성공")
    void modifyRoleUser_FromDraftToUser_Success() {
        // given
        User user = createUserWithSingleJobCategory(passwordEncoder);
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_DRAFT);

        // when
        user.modifyRoleUser();

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("modifyRoleUser: 이미 ROLE_USER인 경우 IllegalStateException 발생")
    void modifyRoleUser_AlreadyUser_ThrowsException() {
        // given
        User user = createUserWithRoleUser(passwordEncoder);

        // when & then
        assertThatThrownBy(() -> user.modifyRoleUser())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 ROLE_USER 권한을 가지고 있습니다");
    }
}
