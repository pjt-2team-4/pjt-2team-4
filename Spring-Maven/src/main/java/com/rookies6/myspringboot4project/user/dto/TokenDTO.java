package com.rookies6.myspringboot4project.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class TokenDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {

        private String accessToken;
        private String tokenType;
        private String email;
    }
}
