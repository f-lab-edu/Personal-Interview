package com.personal.interview.global.security.dto;

import java.time.LocalDateTime;

/**
 * JWT 토큰과 만료시간을 함께 전달하기 위한 DTO입니다.
 * 토큰 생성 시점의 만료시간을 단일 진실 소스(Single Version of Truth)로 사용합니다.
 */
public record TokenWithExpiry(String token, LocalDateTime expiresAt) {
}
