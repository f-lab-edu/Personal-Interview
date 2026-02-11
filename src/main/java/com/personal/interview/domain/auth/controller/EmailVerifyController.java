package com.personal.interview.domain.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.personal.interview.domain.auth.controller.dto.EmailVerifyResponse;
import com.personal.interview.domain.auth.service.VerifyService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailVerifyController {

	private final VerifyService verifyService;

	@PostMapping("/send")
	public ResponseEntity<EmailVerifyResponse> sendVerificationEmail() {
		UserId userId = SecurityUtil.getCurrentUserId();

		return ResponseEntity
				.ok(EmailVerifyResponse.from(verifyService.sendVerifyEmail(userId)));
	}

	@GetMapping("/verify")
	public ResponseEntity<EmailVerifyResponse> verifyEmail(@RequestParam String token) {
		UUID uuidToken;
		try {
			uuidToken = UUID.fromString(token);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
		}

		return ResponseEntity
				.ok(EmailVerifyResponse.from(verifyService.verifyEmail(uuidToken)));
	}

	/**
	 * 이메일 인증 링크를 통한 인증 처리 (뷰 렌더링 방식)
	 * 
	 * @param token 인증 토큰
	 * @return 인증 성공 시 성공 페이지, 실패 시 실패 페이지를 직접 렌더링
	 */
	@GetMapping("/verify/redirect")
	public ModelAndView verifyEmailWithRedirect(@RequestParam String token) {
		try {
			UUID uuidToken = UUID.fromString(token);
			verifyService.verifyEmail(uuidToken);

			return new ModelAndView("email/verify-success");

		} catch (DomainException e) {
			ModelAndView mav = new ModelAndView("email/verify-failure");
			mav.addObject("errorMessage", e.getMessage());
			mav.addObject("errorCode", e.getErrorCode().getCode());
			return mav;

		} catch (IllegalArgumentException e) {
			ModelAndView mav = new ModelAndView("email/verify-failure");
			mav.addObject("errorMessage", "유효하지 않은 토큰 형식입니다.");
			mav.addObject("errorCode", "INVALID_TOKEN_FORMAT");
			return mav;
		}
	}
}
