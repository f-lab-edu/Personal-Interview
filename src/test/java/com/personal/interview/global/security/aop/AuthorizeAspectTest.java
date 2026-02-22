package com.personal.interview.global.security.aop;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
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
    private Authorize authorize;

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

    @Test
    @DisplayName("인증 정보가 없으면 DomainException 이 발생한다")
    void checkAuthorize_NoAuth() {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        assertThatThrownBy(() -> authorizeAspect.checkAuthorize(joinPoint, authorize))
            .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Authorize 에 명시된 권한이 빈 배열이면 무조건 통과한다")
    void checkAuthorize_EmptyRoles() {
        // given
        setAuthentication("ROLE_USER");
        given(authorize.value()).willReturn(new UserRole[] {});

        // when & then
        assertThatCode(() -> authorizeAspect.checkAuthorize(joinPoint, authorize))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자가 요구된 권한을 가지고 있으면 통과한다")
    void checkAuthorize_HasRole_Success() {
        // given
        setAuthentication("ROLE_DRAFT");
        given(authorize.value()).willReturn(new UserRole[] { UserRole.ROLE_DRAFT, UserRole.ROLE_USER });

        // when & then
        assertThatCode(() -> authorizeAspect.checkAuthorize(joinPoint, authorize))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자가 요구된 권한이 전혀 없으면 DomainException 이 발생한다")
    void checkAuthorize_HasNoRole_Fail() {
        // given
        setAuthentication("ROLE_USER");
        given(authorize.value()).willReturn(new UserRole[] { UserRole.ROLE_DRAFT });

        // when & then
        assertThatThrownBy(() -> authorizeAspect.checkAuthorize(joinPoint, authorize))
            .isInstanceOf(DomainException.class);
    }
}
