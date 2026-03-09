package com.personal.interview.util.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import com.personal.interview.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SecurityValidatorUtilTest {

    private Validator validator;

    @Mock
    private ObjectMapper objectMapper;

    private SecurityValidatorUtil securityValidatorUtil;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        securityValidatorUtil = new SecurityValidatorUtil(validator, objectMapper);
    }

    static class TestDto {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        public TestDto(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    @Test
    @DisplayName("검증을 통과하면 예외가 발생하지 않는다")
    void validate_Success() {
        // given
        TestDto validDto = new TestDto("test@example.com", "password123");
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when & then
        assertThatCode(() -> securityValidatorUtil.validate(validDto, response))
                .doesNotThrowAnyException();
        verifyNoInteractions(response, objectMapper);
    }

    @Test
    @DisplayName("실제 DTO 객체에서 값이 누락되어 검증에 실패하면 에러 응답 및 예외가 발생한다")
    void validate_FailThrowsException() throws Exception {
        // given
        TestDto invalidDto = new TestDto("", ""); // 이메일, 비밀번호 모두 Blank
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter printWriter = mock(PrintWriter.class);

        given(response.getWriter()).willReturn(printWriter);

        // when & then
        assertThatThrownBy(() -> securityValidatorUtil.validate(invalidDto, response))
                .isInstanceOf(SecurityFilterValidationException.class);
        // Blank 위반 메시지 중 하나가 나올 것

        verify(response).setStatus(ErrorCode.INVALID_CREDENTIALS.getStatus().value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(objectMapper).writeValue(eq(printWriter), any());
    }
}
