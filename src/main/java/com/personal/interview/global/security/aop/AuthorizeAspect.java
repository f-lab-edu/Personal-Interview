package com.personal.interview.global.security.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.personal.interview.domain.user.entity.vo.UserRole;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.global.security.annotation.Authorize;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class AuthorizeAspect {

    @Before("@annotation(authorize)")
    public void checkAuthorize(JoinPoint joinPoint, Authorize authorize) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw DomainException.create(ErrorCode.ACCESS_DENIED);
        }

        UserRole[] requiredRoles = authorize.value();
        if (requiredRoles.length == 0) {
            return;
        }

        boolean hasRole = Arrays.stream(requiredRoles)
                .map(Enum::name)
                .anyMatch(role -> authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(role)));

        if (!hasRole) {
            log.warn("권한이 부족합니다. (요구 권한: {}, 현재 사용자 권한: {})",
                    Arrays.toString(requiredRoles), authentication.getAuthorities());
            throw DomainException.create(ErrorCode.ACCESS_DENIED);
        }
    }
}
