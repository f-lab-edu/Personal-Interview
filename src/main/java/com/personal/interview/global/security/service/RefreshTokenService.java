package com.personal.interview.global.security.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.personal.interview.domain.auth.entity.UserRefreshToken;
import com.personal.interview.domain.auth.repository.UserRefreshTokenRepository;
import com.personal.interview.domain.auth.service.AuthService;
import com.personal.interview.domain.user.entity.UserId;
import com.personal.interview.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final UserRefreshTokenRepository userRefreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public void rotateRefreshToken(UserId userId, String refreshToken) {
		String hashedToken = AuthService.hashToken(refreshToken);

		// 2. 만료 시간 계산
		LocalDateTime expiryAt = LocalDateTime.now()
			.plusSeconds(jwtTokenProvider.getRefreshExpirationMs() / 1000);

		// 3. 존재하면 업데이트, 없으면 새로 생성 (RTR)
		userRefreshTokenRepository.findByUserId(userId)
			.ifPresentOrElse(
				existing -> existing.updateRefreshToken(hashedToken, expiryAt),
				() -> userRefreshTokenRepository.save(UserRefreshToken.create(userId, hashedToken, expiryAt))
			);
	}
}