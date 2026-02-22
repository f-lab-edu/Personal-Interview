package com.personal.interview.global.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.personal.interview.domain.user.entity.vo.UserRole;

/**
 * Controller 메서드나 클래스에 선언하여 지정된 UserRole 만 접근할 수 있도록 인가 검증을 수행하는 어노테이션
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorize {
    /**
     * 허용할 권한 목록
     */
    UserRole[] value();
}
