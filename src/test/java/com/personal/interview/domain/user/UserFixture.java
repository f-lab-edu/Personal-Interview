package com.personal.interview.domain.user;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import com.personal.interview.domain.user.entity.dto.SignUpRequest;
import com.personal.interview.domain.user.entity.dto.SignUpResponse;
import com.personal.interview.domain.user.entity.User;
import com.personal.interview.domain.user.entity.vo.JobCategoryName;
import com.personal.interview.domain.user.entity.vo.UserRole;

public class UserFixture {

    // 기본 사용자 정보
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_RAW_PASSWORD = "password123";
    public static final String DEFAULT_ENCODED_PASSWORD = "$2a$10$encoded_password123";
    public static final String DEFAULT_NICKNAME = "테스터";

    // 이메일 관련
    public static final String OLD_EMAIL = "old@example.com";
    public static final String NEW_EMAIL = "new@example.com";

    // Job Category 관련
    public static final JobCategoryName JOB_BACKEND = JobCategoryName.BACKEND;
    public static final JobCategoryName JOB_DEVOPS = JobCategoryName.DEVOPS;
    public static final List<JobCategoryName> JOB_CATEGORIES_BACKEND_DEVOPS = List.of(JOB_BACKEND, JOB_DEVOPS);
    public static final List<JobCategoryName> JOB_CATEGORIES_BACKEND = List.of(JOB_BACKEND);

    // 예상 크기
    public static final int EXPECTED_JOB_COUNT_TWO = 2;

    public static SignUpRequest createDefaultSignUpRequest() {
        return new SignUpRequest(
                OLD_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND_DEVOPS);
    }

    public static SignUpRequest createSignUpRequestWithSingleJobCategory() {
        return new SignUpRequest(
                OLD_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND);
    }

    public static SignUpRequest createSignUpRequestWithOldEmail() {
        return new SignUpRequest(
                OLD_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND);
    }

    public static User createDefaultUser(PasswordEncoder passwordEncoder) {
        User user = User.signUp(createDefaultSignUpRequest(), passwordEncoder);
        user.modifyEmail(DEFAULT_EMAIL);
        return user;
    }

    public static User createUserWithSingleJobCategory(PasswordEncoder passwordEncoder) {
        User user = User.signUp(createSignUpRequestWithSingleJobCategory(), passwordEncoder);
        user.modifyEmail(DEFAULT_EMAIL);
        return user;
    }

    public static User createUserWithOldEmail(PasswordEncoder passwordEncoder) {
        User user = User.signUp(createSignUpRequestWithOldEmail(), passwordEncoder);
        user.modifyEmail(OLD_EMAIL);
        return user;
    }

    public static User createUserWithRoleUser(PasswordEncoder passwordEncoder) {
        User user = createDefaultUser(passwordEncoder);
        user.modifyRoleUser();
        return user;
    }

    public static SignUpRequest createSignUpRequest() {
        return new SignUpRequest(
                OLD_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND_DEVOPS);
    }

    public static SignUpResponse createSignUpResponse(SignUpRequest request) {
        return new SignUpResponse(
                1L,
                request.nickname(),
                UserRole.ROLE_DRAFT,
                request.jobCategoryNames(),
                LocalDateTime.now());
    }
}
