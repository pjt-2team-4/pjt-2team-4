package com.rookies6.myspringboot4project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    RESOURCE_NOT_FOUND(
            "해당 자원을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    STUDENT_NUMBER_DUPLICATE(
            "이미 존재하는 학번입니다: %s",
            HttpStatus.CONFLICT
    ),

    EMAIL_DUPLICATE(
            "이미 사용 중인 이메일입니다: %s",
            HttpStatus.CONFLICT
    ),

    PHONE_NUMBER_DUPLICATE(
            "이미 사용 중인 전화번호입니다: %s",
            HttpStatus.CONFLICT
    ),

    INVALID_INPUT(
            "입력값이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),

    INTERNAL_SERVER_ERROR(
            "서버 오류가 발생했습니다.",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
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
