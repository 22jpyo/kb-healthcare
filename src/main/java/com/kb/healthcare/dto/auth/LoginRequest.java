package com.kb.healthcare.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "이메일 주소", example = "user@example.com")
        @Email @NotBlank String email,

        @Schema(description = "비밀번호 (8~64자)", example = "password123!")
        @NotBlank @Size(min = 8, max = 64) String password
) {
}