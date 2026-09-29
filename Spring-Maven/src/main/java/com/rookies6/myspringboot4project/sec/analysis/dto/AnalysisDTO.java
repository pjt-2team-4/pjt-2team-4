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

    // 새로운 엔티티 스키마에 맞춰 DTO 필드 변경
    @Getter
    public static class VulnerabilityDto {
        private final Long id;
        private final Long ruleId;
        private final String severity;
        private final Integer startLine;
        private final Integer endLine;
        private final String codeSnippet;

        public VulnerabilityDto(VulnerabilityFinding finding) {
            this.id = finding.getId();
            this.ruleId = finding.getRuleId();
            this.severity = finding.getSeverity();
            this.startLine = finding.getStartLine();
            this.endLine = finding.getEndLine();
            this.codeSnippet = finding.getCodeSnippet();
        }
    }
}