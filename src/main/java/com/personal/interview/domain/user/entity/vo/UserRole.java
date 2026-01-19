package com.personal.interview.domain.user.entity.vo;

/**
 * 사용자 권한 Enum
 * - ROLE_DRAFT: 임시 회원 (이메일 미인증)
 * - ROLE_USER: 정상 회원 (이메일 인증 완료)
 */
public enum UserRole {
    ROLE_DRAFT, // 임시 회원
    ROLE_USER // 정상 회원
}
