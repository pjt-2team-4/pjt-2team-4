package com.rookies6.myspringboot4project.user.dto;

import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class LoginDTO {
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
        private String password;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class Response {
        private final Long id;
        private final String email;
        private final String accessToken;
        private final String tokenType;
        private final long expiresIn;

        public static LoginDTO.Response of(User user,String accessToken, long expiresInSeconds) {
            return Response.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresInSeconds)
                    .build();
        }
    }
}
