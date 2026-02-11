package com.personal.interview.domain.auth.service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.personal.interview.domain.user.entity.vo.Email;
import com.personal.interview.global.config.properties.AuthProperties;
import com.personal.interview.global.config.properties.EmailProperties;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerifySenderImpl implements EmailVerifySender {
    
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties emailProperties;
    private final AuthProperties authProperties;

    @Override
    public void sendVerificationEmail(Email email, UUID token) {
        try {
            MimeMessage mimeMessage = setMessage(email, token);

            javaMailSender.send(mimeMessage);
            
            log.info("이메일 발송 성공: email={}, token={}", email.value(), token);
            
        } catch (Exception e) {
            log.error("이메일 발송 실패: email={}, token={}, error={}", 
                email.value(), token, e.getMessage(), e);
        }
    }

    private MimeMessage setMessage(Email email, UUID token) throws
        MessagingException,
        UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(emailProperties.senderEmail(), emailProperties.senderName());

        helper.setTo(email.value());

        helper.setSubject("[Personal Interview] 이메일 인증을 완료해주세요");

        String htmlContent = generateEmailContent(token);
        helper.setText(htmlContent, true);
        return mimeMessage;
    }

    /**
     * Thymeleaf 템플릿을 사용하여 HTML 이메일 콘텐츠 생성
     */
    private String generateEmailContent(UUID token) {
        Context context = new Context();
        
        // 인증 링크 생성
        String verificationLink = String.format("%s/api/auth/email/verify/redirect?token=%s",
            emailProperties.baseUrl(), token);
        
        context.setVariable("verificationLink", verificationLink);
        context.setVariable("expirationMinutes", authProperties.verificationExpirationMinutes());
        
        return templateEngine.process("email/verification", context);
    }
}
