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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "유효하지 않은 인증 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_005", "이메일 또는 비밀번호가 올바르지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_006", "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_007", "존재하지 않는 리프레시 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_008", "접근 권한이 없습니다."),
    REFRESH_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "AUTH_009", "토큰 재사용이 감지되어 모든 세션이 종료되었습니다. 다시 로그인해 주세요."),

    // User
    ALREADY_ROLE_USER(HttpStatus.CONFLICT, "USER_001", "이미 해당 권한을 가진 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER_002", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_003", "존재하지 않는 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
