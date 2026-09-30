package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.FindingVulnerability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisDTO {

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

    @Getter
    public static class Response {

        private final Long analysisId;
        private final String title;
        private final String language;
        private final String status;

        private final int totalCount;
        private final long criticalCount;
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

            List<FindingVulnerability> allFindings = request.getAnalysisFiles().stream()
                    .flatMap(file -> file.getFindings().stream())
                    .toList();

            this.totalCount = allFindings.size();

            // Severity Enum 비교를 통한 데이터 집계
            this.criticalCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.CRITICAL).count();
            this.highCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.HIGH).count();
            this.mediumCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.MEDIUM).count();
            this.lowCount = allFindings.stream().filter(f -> f.getSeverity() == Severity.LOW).count();

            this.vulnerabilities = allFindings.stream()
                    .map(VulnerabilityDto::new)
                    .toList();

            this.createdAt = request.getCreatedAt();
        }

        public static Response fromEntity(AnalysisRequest request) {
            return new Response(request);
        }
    }

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

    @Getter
    public static class VulnerabilityDto {
        private final Long id;
        private final Long fileId;
        private final String fileName;
        private final String ruleId;
        private final String type;
        private final String typeDisplayName;
        private final String severity;
        private final String severityDisplayName;
        private final String description;
        private final Integer startLine;
        private final Integer endLine;
        private final String codeSnippet;

        public VulnerabilityDto(FindingVulnerability finding) {
            this.id = finding.getId();
            this.fileId = finding.getAnalysisFile() != null ? finding.getAnalysisFile().getId() : null;
            this.fileName = finding.getAnalysisFile() != null ? finding.getAnalysisFile().getFileName() : null;
            this.ruleId = finding.getRuleId();
            
            // VulnerabilityType Enum 정보 매핑
            this.type = finding.getVulnerabilityType() != null ? finding.getVulnerabilityType().name() : null;
            this.typeDisplayName = finding.getVulnerabilityType() != null ? finding.getVulnerabilityType().getDisplayName() : null;
            
            // Severity Enum 정보 매핑
            this.severity = finding.getSeverity() != null ? finding.getSeverity().name() : null;
            this.severityDisplayName = finding.getSeverity() != null ? finding.getSeverity().getDisplayName() : null;
            
            // description 필드가 제거되었으므로 빈 값(또는 필요한 대체 값) 처리
            this.description = ""; 
            this.startLine = finding.getStartLine();
            this.endLine = finding.getEndLine();
            this.codeSnippet = finding.getCodeSnippet();
        }
    }
}