package com.kb.healthcare.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank String name,

        @Schema(description = "닉네임", example = "건강")
        @NotBlank String nickname,

        @Schema(description = "이메일 주소", example = "user@example.com")
        @Email @NotBlank String email,

        @Schema(description = "비밀번호 (8~64자)", example = "password123!")
        @NotBlank @Size(min = 8, max = 64) String password,

        @Schema(description = "건강 기록 키 (고유 식별자)", example = "3b87c9a4-f983-4168-8f27-85436447bb57")
        @NotBlank String recordKey
) {
}