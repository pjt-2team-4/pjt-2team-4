package com.rookies6.myspringboot4project.common.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ApiResponse<T>(boolean success, T data, String message, String timestamp) {
    public static <T> ApiResponse<T> success(T data) {
        private static final String DEFAULT_MESSAGE = "요청이 성공적으로 처리되었습니다";
        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public static <T> ApiResponse<T> success(T data) {
            return success(data, DEFAULT_MESSAGE);
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message, LocalDateTime.now().format(FORMAT));
        }
    }
}