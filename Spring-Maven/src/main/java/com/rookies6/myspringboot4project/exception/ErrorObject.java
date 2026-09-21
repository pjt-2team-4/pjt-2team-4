package com.rookies6.myspringboot4project.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
public class ErrorObject {
    private Integer statusCode;
    private String message;
    private String timestamp;

    public String getTimestamp() {
        LocalDateTime ldt = LocalDateTime.now();
        return DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss",
                Locale.KOREA).format(ldt);
    }
}