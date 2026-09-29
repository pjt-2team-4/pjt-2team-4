package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.VulnerabilityFinding;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisDTO {

    /**
     * 분석 요청
     */
    @Getter
    @NoArgsConstructor
    public static class Request {

        @NotBlank(message = "분석 제목(Title)은 필수입니다.")
        private String title;

        @NotBlank(message = "언어 설정은 필수입니다.")
        private String language;

        @NotNull(message = "분석할 파일 목록은 필수입니다.")
        private List<FileRequest> files;

        @Getter
        @NoArgsConstructor
        public static class FileRequest {
            @NotBlank(message = "파일 경로는 필수입니다.")
            private String filePath;

            @NotBlank(message = "파일 내용은 필수입니다.")
            private String content;
        }
    }

    /**
     * 분석 결과
     */
    @Getter
    public static class Response {

        private final Long analysisId;
        private final String title;
        private final String language;
        private final String status;

        private final int totalCount;
        private final long highCount;
        private final long mediumCount;
        private final long lowCount;

        private final List<VulnerabilityDto> vulnerabilities;

        private final LocalDateTime createdAt;

        public Response(AnalysisRequest request) {
            this.analysisId = request.getId();
            this.title = request.getTitle();
            this.language = request.getLanguage();
            this.status = request.getStatus().name();

            List<VulnerabilityFinding> allFindings = request.getAnalysisFiles().stream()
                    .flatMap(file -> file.getFindings().stream())
                    .toList();

            this.totalCount = allFindings.size();

            this.highCount = allFindings.stream()
                    .filter(f -> f.getSeverity() != null && "HIGH".equalsIgnoreCase(f.getSeverity().name()))
                    .count();

            this.mediumCount = allFindings.stream()
                    .filter(f -> f.getSeverity() != null && "MEDIUM".equalsIgnoreCase(f.getSeverity().name()))
                    .count();

            this.lowCount = allFindings.stream()
                    .filter(f -> f.getSeverity() != null && "LOW".equalsIgnoreCase(f.getSeverity().name()))
                    .count();

            this.vulnerabilities = allFindings.stream()
                    .map(VulnerabilityDto::new)
                    .toList();

            this.createdAt = request.getCreatedAt();
        }

        public static Response fromEntity(AnalysisRequest request) {
            return new Response(request);
        }
    }

    /**
     * 분석 상태
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class StatusResponse {
        private final Long analysisId;
        private final String status;
        private final String errorMessage;

        public static StatusResponse fromEntity(AnalysisRequest request) {
            return StatusResponse.builder()
                    .analysisId(request.getId())
                    .status(request.getStatus().name())
                    .errorMessage(request.getErrorMessage())
                    .build();
        }
    }

    /**
     * 취약점 정보
     */
    @Getter
    public static class VulnerabilityDto {
        private final Long id;
        private final Long fileId;
        private final String fileName;
        private final String ruleId;
        private final String type;
        private final String severity;
        private final String description;
        private final Integer startLine;
        private final Integer endLine;
        private final String codeSnippet;

        public VulnerabilityDto(VulnerabilityFinding finding) {
            this.id = finding.getId();
            this.fileId = finding.getAnalysisFile() != null ? finding.getAnalysisFile().getId() : null;
            this.fileName = finding.getAnalysisFile() != null ? finding.getAnalysisFile().getFileName() : null;
            this.ruleId = finding.getRuleId();
            this.type = finding.getVulnerabilityType();
            this.severity = finding.getSeverity() != null ? finding.getSeverity().name() : null;
            this.description = finding.getDescription();
            this.startLine = finding.getStartLine();
            this.endLine = finding.getEndLine();
            this.codeSnippet = finding.getCodeSnippet();
        }
    }

    // ==========================================
    // 💡 아래 두 DTO 클래스가 누락되어 추가했습니다.
    // ==========================================

    /**
     * 파일 목록 조회용 응답 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileResponse {
        private Long id;
        private String name;
        private String relativePath;
    }

    /**
     * 파일 상세 내용 조회용 응답 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileDetailResponse {
        private Long id;
        private String name;
        private String content;
    }
}