package com.rookies6.myspringboot4project.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode {

    EMAIL_NOT_FOUND(
            "해당 이메일을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    EMAIL_DUPLICATE(
            "이미 사용 중인 이메일입니다: %s",
            HttpStatus.CONFLICT
    ),


    INVALID_INPUT(
            "이메일 또는 비밀번호 형식이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    );

    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage(Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        return String.format(message, args);
    }
}
