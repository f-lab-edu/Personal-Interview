package com.personal.interview.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 이메일 발송 관련 설정값을 관리하는 Properties 클래스
 */
@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
        @DefaultValue("http://localhost:8080") String baseUrl,
        @DefaultValue("noreply@personal-interview.com") String senderEmail,
        @DefaultValue("Personal Interview") String senderName) {
}
