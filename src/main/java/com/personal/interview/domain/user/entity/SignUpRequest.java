package com.personal.interview.domain.user.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 255, message = "이메일은 최대 255자입니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(max = 64, message = "비밀번호는 최대 64자입니다")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 50, message = "닉네임은 최대 50자입니다")
    private String nickname;

    @NotEmpty(message = "최소 하나의 직무 카테고리를 선택해야 합니다")
    private List<String> jobCategoryNames;
}
