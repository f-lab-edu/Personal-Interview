package com.personal.interview.domain.user.controller.dto;

import java.util.List;

import com.personal.interview.domain.user.entity.vo.JobCategoryName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다") @Size(max = 100, message = "이메일은 최대 100자입니다") @Email(message = "유효한 이메일 형식이어야 합니다") String email,

        @NotBlank(message = "비밀번호는 필수입니다") @Size(min = 8, max = 64, message = "비밀번호는 최소 8자 이상, 최대 64자 이하여야 합니다") String password,

        @NotBlank(message = "닉네임은 필수입니다") @Size(max = 50, message = "닉네임은 최대 50자입니다") String nickname,

		@NotEmpty(message = "최소 하나의 직무 카테고리를 선택해야 합니다")
		List<JobCategoryName> jobCategoryNames) {
}
