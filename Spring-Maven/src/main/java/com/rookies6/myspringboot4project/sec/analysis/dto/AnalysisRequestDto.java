package com.rookies6.myspringboot4project.sec.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequestDto {

    @NotBlank(message = "분석 제목(Title)은 필수입니다.")
    private String title;

    @NotBlank(message = "언어 설정은 필수입니다.")
    private String language;

    @NotNull(message = "분석할 파일 목록은 필수입니다.")
    private List<FileRequestDto> files;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileRequestDto {

        @NotBlank(message = "파일 경로는 필수입니다.")
        private String filePath;

        @NotBlank(message = "파일 내용은 필수입니다.")
        private String content;
    }
}