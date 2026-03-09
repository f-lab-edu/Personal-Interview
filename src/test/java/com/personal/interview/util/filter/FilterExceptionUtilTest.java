package com.personal.interview.util.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import com.personal.interview.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FilterExceptionUtilTest {

    @Mock
    private HttpServletResponse response;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PrintWriter printWriter;

    @Test
    @DisplayName("sendErrorResponse (기본 메시지) - 상태 코드, Content-Type 설정 및 직렬화 호출 확인")
    void sendErrorResponse_DefaultMessage() throws Exception {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;
        when(response.getWriter()).thenReturn(printWriter);

        // when
        FilterExceptionUtil.sendErrorResponse(response, errorCode, objectMapper);

        // then
        verify(response).setStatus(errorCode.getStatus().value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(objectMapper).writeValue(eq(printWriter), any());
    }

    @Test
    @DisplayName("sendErrorResponse (커스텀 메시지) - 상태 코드, Content-Type 설정 및 직렬화 호출 확인")
    void sendErrorResponse_CustomMessage() throws Exception {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;
        String customMessage = "커스텀 에러 메시지입니다.";
        when(response.getWriter()).thenReturn(printWriter);

        // when
        FilterExceptionUtil.sendErrorResponse(response, errorCode, customMessage, objectMapper);

        // then
        verify(response).setStatus(errorCode.getStatus().value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(objectMapper).writeValue(eq(printWriter), any());
    }
}
