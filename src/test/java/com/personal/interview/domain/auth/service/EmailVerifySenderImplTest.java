package com.personal.interview.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.global.config.properties.AuthProperties;
import com.personal.interview.global.config.properties.EmailProperties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailVerifySenderImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private EmailVerifySenderImpl emailVerifySender;

    private Email testEmail;
    private UUID testToken;
    private MimeMessage mockMimeMessage;

    @BeforeEach
    void setUp() {
        testEmail = new Email("test@example.com");
        testToken = UUID.randomUUID();
        mockMimeMessage = new MimeMessage((Session) null);

        // EmailProperties Mock 설정
        given(emailProperties.baseUrl()).willReturn("http://localhost:8080");
        given(emailProperties.senderEmail()).willReturn("noreply@personal-interview.com");
        given(emailProperties.senderName()).willReturn("Personal Interview");

        // AuthProperties Mock 설정
        given(authProperties.verificationExpirationMinutes()).willReturn(10);

        // JavaMailSender Mock 설정
        given(mailSender.createMimeMessage()).willReturn(mockMimeMessage);

        // TemplateEngine Mock 설정
        given(templateEngine.process(eq("email/verification"), any())).willReturn("<html>Test Email</html>");
    }

    @Test
    @DisplayName("이메일 발송 성공")
    void sendVerificationEmail_Success() {
        // given
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        assertThatCode(() -> emailVerifySender.sendVerificationEmail(testEmail, testToken))
            .doesNotThrowAnyException();

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(templateEngine, times(1)).process(eq("email/verification"), any());
    }


    @Test
    @DisplayName("Thymeleaf 템플릿에 올바른 변수가 전달되는지 검증")
    void sendVerificationEmail_TemplateVariablesCorrect() {
        // given
        ArgumentCaptor<org.thymeleaf.context.Context> contextCaptor = 
            ArgumentCaptor.forClass(org.thymeleaf.context.Context.class);

        // when
        emailVerifySender.sendVerificationEmail(testEmail, testToken);

        // then
        verify(templateEngine).process(eq("email/verification"), contextCaptor.capture());

        org.thymeleaf.context.Context capturedContext = contextCaptor.getValue();
        String verificationLink = (String) capturedContext.getVariable("verificationLink");
        Integer expirationMinutes = (Integer) capturedContext.getVariable("expirationMinutes");

        assertThat(verificationLink).contains("http://localhost:8080");
        assertThat(verificationLink).contains("/api/auth/email/verify/redirect");
        assertThat(verificationLink).contains(testToken.toString());
        assertThat(expirationMinutes).isEqualTo(10);
    }

    @Test
    @DisplayName("이메일 발송이 실패하면 retry 동작 검증")
    void retryTest() {
        // 첫 2번은 예외 발생, 3번째는 정상 실행(doNothing) 설정
        doThrow(new RuntimeException("Mail Server Down"))
            .doThrow(new RuntimeException("Connection Timeout"))
            .doNothing()
            .when(mailSender).send(any(MimeMessage.class));

        // When
        emailVerifySender.sendVerificationEmail(testEmail, testToken);

        // Then
        verify(mailSender, times(3)).send(any(MimeMessage.class));
    }


    @Test
    @DisplayName("retry 최대 시도 횟수 검증")
    void retryFailTest() {
        // 6번 모두 예외 발생 설정
        doThrow(new RuntimeException("Mail Server Down"))
            .doThrow(new RuntimeException("Connection Timeout"))
            .doThrow(new RuntimeException("Connection Timeout"))
            .doThrow(new RuntimeException("Connection Timeout"))
            .doThrow(new RuntimeException("Connection Timeout"))
            .doThrow(new RuntimeException("Connection Timeout"))
            .when(mailSender).send(any(MimeMessage.class));

        // When
        assertThatCode(() -> emailVerifySender.sendVerificationEmail(testEmail, testToken))
            .doesNotThrowAnyException();

        // Then
        verify(mailSender, times(5)).send(any(MimeMessage.class));
    }
}
