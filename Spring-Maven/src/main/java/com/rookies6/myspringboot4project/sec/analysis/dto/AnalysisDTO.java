package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.VulnerabilityFinding;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisDTO {

    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotNull(message = "프로젝트 ID는 필수입니다.")
        private Long projectId;

        @NotBlank(message = "언어 설정은 필수입니다.")
        private String language;

        @NotBlank(message = "분석할 코드는 필수입니다.")
        private String code;
    }

    @Getter
    public static class Response {
        private final Long analysisId;
        private final Long projectId;
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
            this.projectId = request.getProject().getId();
            this.language = request.getLanguage();
            this.status = request.getStatus();
            this.totalCount = request.getFindings().size();
            this.highCount = request.getFindings().stream().filter(f -> "HIGH".equals(f.getSeverity())).count();
            this.mediumCount = request.getFindings().stream().filter(f -> "MEDIUM".equals(f.getSeverity())).count();
            this.lowCount = request.getFindings().stream().filter(f -> "LOW".equals(f.getSeverity())).count();
            this.vulnerabilities = request.getFindings().stream().map(VulnerabilityDto::new).toList();
            this.createdAt = request.getCreatedAt();
        }

        public static Response fromEntity(AnalysisRequest request) {
            return new Response(request);
        }
    }

    @Getter
    public static class VulnerabilityDto {
        private final Long id;
        private final String type;
        private final String severity;
        private final int line;
        private final String problemCode;
        private final String description;
        private final String aiExplanation;
        private final String afterCode;

        public VulnerabilityDto(VulnerabilityFinding finding) {
            this.id = finding.getId();
            this.type = finding.getType();
            this.severity = finding.getSeverity();
            this.line = finding.getLineNumber();
            this.problemCode = finding.getProblemCode();
            this.description = finding.getDescription();
            this.aiExplanation = finding.getAiExplanation();
            this.afterCode = finding.getAfterCode();
        }
    }
}