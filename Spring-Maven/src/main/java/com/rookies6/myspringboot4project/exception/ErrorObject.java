package com.rookies6.myspringboot4project.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorObject {
    private boolean success = false;
    private Integer statusCode;
    private String errorCode;
    private String message;
    private String timestamp;

    public String getTimestamp() {
        LocalDateTime ldt = LocalDateTime.now();
        return DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss",
                Locale.KOREA).format(ldt);
    }
}
