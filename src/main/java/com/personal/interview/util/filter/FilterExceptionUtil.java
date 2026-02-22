package com.personal.interview.util.filter;

import java.io.IOException;

import org.springframework.http.MediaType;

import com.personal.interview.global.exception.ErrorCode;
import com.personal.interview.global.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Filter 및 Security Handler 계층에서 예외나 인증 실패가 발생했을 때
 * 일관된 JSON 형식의 에러 응답을 작성하기 위한 유틸리티 클래스입니다.
 */
public class FilterExceptionUtil {

    /**
     * 기본 ErrorCode의 메시지를 사용하여 에러 응답을 작성합니다.
     *
     * @param response     HttpServletResponse 객체
     * @param errorCode    에러 코드 객체 (상태 코드, 코드, 메시지 포함)
     * @param objectMapper ErrorResponse 객체를 JSON으로 직렬화하기 위한 ObjectMapper
     * @throws IOException 스트림에 쓰는 과정에서 발생할 수 있는 I/O 예외
     */
    public static void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode, ObjectMapper objectMapper)
            throws IOException {
        sendErrorResponse(response, errorCode, errorCode.getMessage(), objectMapper);
    }

    /**
     * 커스텀 메시지를 사용하여 에러 응답을 작성합니다.
     * Validator 등을 통해 생성된 구체적인 에러 사유를 사용할 때 적합합니다.
     *
     * @param response     HttpServletResponse 객체
     * @param errorCode    에러 코드 객체 (상태 코드, 식별용 코드 문자열 포함)
     * @param message      에러 응답에 포함될 구체적인 커스텀 메시지
     * @param objectMapper ErrorResponse 객체를 JSON으로 직렬화하기 위한 ObjectMapper
     * @throws IOException 스트림에 쓰는 과정에서 발생할 수 있는 I/O 예외
     */
    public static void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message,
            ObjectMapper objectMapper) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .build();

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
