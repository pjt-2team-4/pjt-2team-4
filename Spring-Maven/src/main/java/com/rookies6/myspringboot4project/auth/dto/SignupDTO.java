package com.rookies6.myspringboot4project.auth.dto;

import com.rookies6.myspringboot4project.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SignupDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max=100)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=\\S+$)(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]).{8,64}$",
                message = "비밀번호는 8~64자이며 영문자, 숫자, 특수문자를 각각 1개 이상 포함하고 공백이 없어야 합니다."
        )
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private String email;
        // 보안상 password는 응답에 포함하지 않음

        public static SignupDTO.Response fromEntity(User user) {
            return SignupDTO.Response.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .build();
        }
    }
}
