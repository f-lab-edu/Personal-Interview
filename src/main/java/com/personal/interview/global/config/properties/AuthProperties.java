package com.personal.interview.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 인증 관련 설정값을 관리하는 Properties 클래스
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
	@DefaultValue("5") int maxSendCount,
	@DefaultValue("0") int minSendCount,
	@DefaultValue("10") int verificationExpirationMinutes,
	@DefaultValue("24") int countResetHours
) {
}
