package com.rookies6.myspringboot4project.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    VALIDATION_ERROR(
            "입력값 검증 오류입니다.",
            HttpStatus.BAD_REQUEST
    ),

    RESOURCE_NOT_FOUND(
            "해당 자원을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    FINDING_NOT_FOUND(
            "취약점을 찾을 수 없습니다",
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

    // [수정 완료] AnalysisRequest 검증을 위한 에러 코드 추가
    FILE_COUNT_EXCEEDED(
            "최대 업로드 가능한 파일 개수를 초과했습니다.",
            HttpStatus.PAYLOAD_TOO_LARGE
    ),

    EMPTY_FILE_LIST(
            "분석할 파일이 존재하지 않습니다.",
            HttpStatus.BAD_REQUEST
    ),

    FILE_SIZE_EXCEEDED(
            "파일 1개가 100KB를 초과했습니다.",
            HttpStatus.PAYLOAD_TOO_LARGE
    ),

    CODE_SIZE_EXCEEDED(
            "전체 코드 크기가 500KB를 초과했습니다.",
            HttpStatus.PAYLOAD_TOO_LARGE
    ),

    UNSUPPORTED_LANGUAGE(
            "지원하지 않는 파일 형식입니다: %s",
            HttpStatus.BAD_REQUEST
    ),

    DUPLICATE_FILE_PATH(
            "중복된 파일 경로가 존재합니다.",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_ANALYSIS_STATUS(
            "유효하지 않은 분석 상태 변경 요청입니다.",
            HttpStatus.BAD_REQUEST
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
