package com.personal.interview.domain.user;

import static com.personal.interview.domain.user.UserFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.domain.user.entity.JobCategory;
import com.personal.interview.domain.user.entity.dto.SignUpRequest;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.vo.UserRole;

class UserTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "$2a$10$encoded_" + invocation.getArgument(0));
    }

    @Test
    void signUp() {
        var request = new SignUpRequest(
            "aorl2313@naver.com",
                "password123",
                "테스터",
                List.of("백엔드", "DevOps"));

        User user = User.signUp(request, passwordEncoder);

        assertThat(user.getEmail()).isEqualTo(new Email(request.email()));
        assertThat(user.getPassword()).isEqualTo(passwordEncoder.encode(request.password()));
        assertThat(user.getNickname()).isEqualTo(request.nickname());
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_DRAFT);
        assertThat(user.getJobCategories()).hasSize(2)
                .extracting(JobCategory::getName)
                .containsExactlyInAnyOrder(request.jobCategoryNames().toArray(new String[0]));
    }

    @Test
    void modifyEmail() {
        User user = createUserWithOldEmail(passwordEncoder);

        user.modifyEmail(NEW_EMAIL);

        assertThat(user.getEmail()).isEqualTo(new Email(NEW_EMAIL));
    }

    @Test
    void modifyRoleUser() {
        User user = createUserWithSingleJobCategory(passwordEncoder);
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_DRAFT);

        user.modifyRoleUser();

        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    void modifyRoleUserFail() {
        User user = createUserWithRoleUser(passwordEncoder);

        assertThatThrownBy(() -> user.modifyRoleUser())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 ROLE_USER 권한을 가지고 있습니다");
    }
}
