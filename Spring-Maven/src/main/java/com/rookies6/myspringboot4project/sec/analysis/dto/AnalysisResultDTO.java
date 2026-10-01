package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisProgressStore;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;


public class AnalysisResultDTO {
    @Getter
    @Builder
    // API 명세서 4.1.2 반영해서 필드 추가해놧음
    public static class StatusResponse {
        private final Long analysisId;
        private final String status;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final String stage;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final String stageLabel;

        private final int progress;
        private final String currentFile;
        private final int processedFiles;
        private final int totalFiles;
        private final int findingsSoFar;
        private final Integer totalFindings;
        private final String errorMessage;
        private final LocalDateTime startedAt;
        private final LocalDateTime completedAt;
        private final List<AnalysisProgressStore.LogLine> recentLogs;
        private final String overallSeverity;
        private final Long durationSeconds;
    }

    /** GET /api/v1/analyses/{id} — 7번 (API 명세서 ㅈ기준 4.1.4) */
    @Getter
    @Builder
    public static class SummaryResponse {
        private final Long analysisId;
        private final String title;
        private final String status;
        private final String overallSeverity;
        private final int totalFiles;
        private final int totalFindings;
        private final Map<String, Long> severityCount;   // {"CRITICAL":3, "HIGH":6, "MEDIUM":9, "LOW":5}
        private final List<TypeCount> typeCount;
        private final List<RuleCount> ruleCount;
        private final double resolutionRate;
        private final Long durationSeconds;
        private final LocalDateTime startedAt;
        private final LocalDateTime completedAt;
    }

    /** typeCount 항목 */
    @Getter
    @AllArgsConstructor
    public static class TypeCount {
        private final String type;           // SQL_INJECTION
        private final String displayName;    // VulnerabilityType.displayName
        private final String cweId;          // CWE-89
        private final long count;

        public static TypeCount of(VulnerabilityType type, long count) {
            return new TypeCount(type.name(), type.getDisplayName(), type.getCweId(), count);
        }
    }

    /** ruleCount 항목 */
    @Getter
    @AllArgsConstructor
    public static class RuleCount {
        private final String ruleId;         // SQLI-003
        private final long count;
    }

    /**
     * severityCount 생성 — 문서 형식대로 대문자 키, CRITICAL → LOW 순서, 0건도 항상 포함
     */
    public static Map<String, Long> severityCountOf(Map<Severity, Long> counts) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Severity severity : List.of(Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW)) {
            result.put(severity.name(), counts.getOrDefault(severity, 0L));
        }
        return result;
    }
}