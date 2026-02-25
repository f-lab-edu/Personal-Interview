package com.personal.interview.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
                String secretKey,
                long accessExpirationMs,
                long refreshExpirationMs,
                long refreshGracePeriodSeconds) {
}
