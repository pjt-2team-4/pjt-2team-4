package com.rookies6.myspringboot4project.sec.dto;

import com.rookies6.myspringboot4project.common.validation.DynamicSize;
import com.rookies6.myspringboot4project.sec.entity.Analysis;
import com.rookies6.myspringboot4project.sec.entity.Vulnerability;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class AnalysisDTO {
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "언어 타입은 필수입니다.")
        @DynamicSize(maxProperty = "analysis.language.max-length", message = "언어 타입 길이가 초과되었습니다.")
        private String language;

        @NotBlank(message = "분석할 코드는 필수입니다.")
        private String code;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long analysisId;
        private String language;
        private int totalCount;
        private int highCount;
        private List<VulnerabilityDTO.Response> vulnerabilities;

        public static Response fromEntity(Analysis analysis) {
            return Response.builder()
                    .analysisId(analysis.getId())
                    .language(analysis.getLanguage())
                    .totalCount(analysis.getTotalCount())
                    .highCount(analysis.getHighCount())
                    .vulnerabilities(analysis.getVulnerabilities() != null ? 
                            analysis.getVulnerabilities().stream()
                            .map(VulnerabilityDTO.Response::fromEntity)
                            .collect(Collectors.toList()) : null)
                    .build();
        }
    }

    public static class VulnerabilityDTO {
        @Data @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Response {
            private Long id;
            private String type;
            private String severity;
            private int lineNumber;
            private String problemCode;
            private String description;
            private String aiExplanation;
            private String afterCode;

            public static Response fromEntity(Vulnerability vulnerability) {
                return Response.builder()
                        .id(vulnerability.getId())
                        .type(vulnerability.getType())
                        .severity(vulnerability.getSeverity())
                        .lineNumber(vulnerability.getLineNumber())
                        .problemCode(vulnerability.getProblemCode())
                        .description(vulnerability.getDescription())
                        .aiExplanation(vulnerability.getAiExplanation())
                        .afterCode(vulnerability.getAfterCode())
                        .build();
            }
        }
    }
}