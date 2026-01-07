package com.personal.interview.domain.user;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import com.personal.interview.domain.user.entity.SignUpRequest;
import com.personal.interview.domain.user.entity.User;

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
    public static final String JOB_BACKEND = "백엔드";
    public static final String JOB_DEVOPS = "DevOps";
    public static final List<String> JOB_CATEGORIES_BACKEND_DEVOPS = List.of(JOB_BACKEND, JOB_DEVOPS);
    public static final List<String> JOB_CATEGORIES_BACKEND = List.of(JOB_BACKEND);

    // 예상 크기
    public static final int EXPECTED_JOB_COUNT_TWO = 2;

    /**
     * 기본 SignUpRequest 생성
     */
    public static SignUpRequest createDefaultSignUpRequest() {
        return new SignUpRequest(
                DEFAULT_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND_DEVOPS);
    }

    /**
     * 단일 Job Category SignUpRequest 생성
     */
    public static SignUpRequest createSignUpRequestWithSingleJobCategory() {
        return new SignUpRequest(
                DEFAULT_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND);
    }

    /**
     * 이전 이메일로 SignUpRequest 생성
     */
    public static SignUpRequest createSignUpRequestWithOldEmail() {
        return new SignUpRequest(
                OLD_EMAIL,
                DEFAULT_RAW_PASSWORD,
                DEFAULT_NICKNAME,
                JOB_CATEGORIES_BACKEND);
    }

    /**
     * 기본 설정으로 User 생성
     */
    public static User createDefaultUser(PasswordEncoder passwordEncoder) {
        return User.signUp(createDefaultSignUpRequest(), passwordEncoder);
    }

    /**
     * 단일 Job Category로 User 생성
     */
    public static User createUserWithSingleJobCategory(PasswordEncoder passwordEncoder) {
        return User.signUp(createSignUpRequestWithSingleJobCategory(), passwordEncoder);
    }

    /**
     * 이전 이메일로 User 생성 (이메일 변경 테스트용)
     */
    public static User createUserWithOldEmail(PasswordEncoder passwordEncoder) {
        return User.signUp(createSignUpRequestWithOldEmail(), passwordEncoder);
    }

    /**
     * ROLE_USER로 승급된 User 생성
     */
    public static User createUserWithRoleUser(PasswordEncoder passwordEncoder) {
        User user = createDefaultUser(passwordEncoder);
        user.modifyRoleUser();
        return user;
    }
}
