package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisProgressStore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


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
}