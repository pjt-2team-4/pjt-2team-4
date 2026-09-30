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

    // AnalysisRequest 검증을 위한 에러 코드 추가
    FILE_COUNT_EXCEEDED(
            "최대 업로드 가능한 파일 개수를 초과했습니다.",
            HttpStatus.BAD_REQUEST
    ),

    DUPLICATE_FILE_PATH(
            "중복된 파일 경로가 존재합니다.",
            HttpStatus.CONFLICT
    ),

    INVALID_ANALYSIS_STATUS(
            "유효하지 않은 분석 상태 변경 요청입니다.",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_INPUT(
            "입력값이 올바르지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),

    // 💡 비밀번호 불일치 에러 코드 추가
    INVALID_PASSWORD(
            "비밀번호가 일치하지 않습니다.",
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