package com.personal.interview.global.security.aop;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Collections;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.personal.interview.domain.user.entity.vo.UserRole;
import com.personal.interview.global.exception.DomainException;
import com.personal.interview.global.security.annotation.Authorize;

@ExtendWith(MockitoExtension.class)
class AuthorizeAspectTest {

    @InjectMocks
    private AuthorizeAspect authorizeAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void setAuthentication(String role) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "principal", "credentials", Collections.singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ── 테스트용 @Authorize 스텁 ──

    @Authorize(UserRole.ROLE_DRAFT)
    static class ClassLevelAuthorized {
        public void someMethod() {
        }
    }

    static class MethodLevelAuthorized {
        @Authorize(UserRole.ROLE_USER)
        public void protectedMethod() {
        }

        @Authorize({})
        public void emptyRoleMethod() {
        }
    }

    private void mockJoinPoint(Object target, String methodName) throws NoSuchMethodException {
        Method method = target.getClass().getDeclaredMethod(methodName);
        given(joinPoint.getSignature()).willReturn(methodSignature);
        given(methodSignature.getMethod()).willReturn(method);
        lenient().when(joinPoint.getTarget()).thenReturn(target);
    }

    @Test
    @DisplayName("인증 정보가 없으면 DomainException이 발생한다")
    void checkAuthorize_NoAuth() throws Exception {
        SecurityContextHolder.clearContext();
        mockJoinPoint(new MethodLevelAuthorized(), "protectedMethod");

        assertThatThrownBy(() -> authorizeAspect.checkAuthorize(joinPoint))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("@Authorize에 명시된 권한이 빈 배열이면 무조건 통과한다")
    void checkAuthorize_EmptyRoles() throws Exception {
        setAuthentication("ROLE_USER");
        mockJoinPoint(new MethodLevelAuthorized(), "emptyRoleMethod");

        assertThatCode(() -> authorizeAspect.checkAuthorize(joinPoint))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("메서드 레벨 @Authorize: 사용자가 요구 권한을 가지면 통과한다")
    void checkAuthorize_MethodLevel_HasRole_Success() throws Exception {
        setAuthentication("ROLE_USER");
        mockJoinPoint(new MethodLevelAuthorized(), "protectedMethod");

        assertThatCode(() -> authorizeAspect.checkAuthorize(joinPoint))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("메서드 레벨 @Authorize: 사용자가 요구 권한이 없으면 DomainException이 발생한다")
    void checkAuthorize_MethodLevel_NoRole_Fail() throws Exception {
        setAuthentication("ROLE_ADMIN");
        mockJoinPoint(new MethodLevelAuthorized(), "protectedMethod");

        assertThatThrownBy(() -> authorizeAspect.checkAuthorize(joinPoint))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("클래스 레벨 @Authorize: 메서드에 어노테이션이 없으면 클래스 레벨을 사용한다")
    void checkAuthorize_ClassLevel_Fallback() throws Exception {
        setAuthentication("ROLE_DRAFT");
        mockJoinPoint(new ClassLevelAuthorized(), "someMethod");

        assertThatCode(() -> authorizeAspect.checkAuthorize(joinPoint))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("클래스 레벨 @Authorize: 사용자 권한이 없으면 DomainException이 발생한다")
    void checkAuthorize_ClassLevel_NoRole_Fail() throws Exception {
        setAuthentication("ROLE_USER");
        mockJoinPoint(new ClassLevelAuthorized(), "someMethod");

        assertThatThrownBy(() -> authorizeAspect.checkAuthorize(joinPoint))
                .isInstanceOf(DomainException.class);
    }
}
