package com.rookies6.myspringboot4project.user.dto;

import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class UserDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
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

        public static Response fromEntity(User user) {
            return Response.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .build();
        }
    }
}