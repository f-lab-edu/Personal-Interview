package com.personal.interview.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseErrorCode {
    
    // Auth
    ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH_001", "이미 인증된 사용자이므로 재전송을 요청할 수 없습니다."),
    EXCEED_MAX_SEND_COUNT(HttpStatus.BAD_REQUEST, "AUTH_002", "일일 인증 횟수를 초과했습니다."),
    VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_003", "인증 시간이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_004", "유효하지 않은 인증 토큰입니다."),
    
    // User
    ALREADY_ROLE_USER(HttpStatus.CONFLICT, "USER_001", "이미 해당 권한을 가진 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER_002", "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
