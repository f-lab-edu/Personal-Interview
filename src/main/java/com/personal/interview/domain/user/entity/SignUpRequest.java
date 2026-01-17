package com.personal.interview.domain.user.entity;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(max = 64, message = "비밀번호는 최대 64자입니다")
        String password,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 50, message = "닉네임은 최대 50자입니다")
        String nickname,

        @NotEmpty(message = "최소 하나의 직무 카테고리를 선택해야 합니다")
        List<String> jobCategoryNames
) {
}
