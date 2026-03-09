package com.personal.interview.util.validator;

import java.io.IOException;
import java.util.Set;

import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.util.filter.FilterExceptionUtil;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Security Filter 레이어 내에서 DTO 유효성 검증을 수행하는 유틸리티 클래스입니다.
 * 일반적인 Controller의 @Valid와 달리, 필터 단에서 발생하는 검증 오류를 가로채어
 * 표준화된 에러 응답(JSON)을 반환하고 인증 프로세스를 중단시킵니다.
 */
@Component
@RequiredArgsConstructor
public class SecurityValidatorUtil {
	private final Validator validator;
	private final ObjectMapper objectMapper;

	/**
	 * 대상 객체(target)의 유효성을 검증합니다.
	 * * @param target   검증할 DTO 객체 (예: LoginRequest)
	 * @param response 에러 발생 시 응답을 작성할 HttpServletResponse
	 * @param <T>      검증 대상의 타입
	 * @throws IOException                      응답 작성 중 발생하는 예외
	 * @throws SecurityFilterValidationException 검증 실패 시 발생하여 필터 체인을 중단시킴
	 */
	public <T> void validate(T target, HttpServletResponse response) throws IOException {
		Set<ConstraintViolation<T>> violations = validator.validate(target);

		if (!violations.isEmpty()) {
			String errorMessage = violations.iterator().next().getMessage();

			ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

			FilterExceptionUtil.sendErrorResponse(response, errorCode, errorMessage, objectMapper);

			throw new SecurityFilterValidationException(errorMessage);
		}
	}
}
