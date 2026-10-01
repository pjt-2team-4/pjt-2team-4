package com.rookies6.myspringboot4project.sec.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private long weeklyAnalysisCount;      // 이번주 분석한 수
        private double weeklyAnalysisDelta;    // 지난주 대비 증감률
        private long totalFindings;            // 지금까지 발견한 취약점 수
        private long criticalCount;            // '치명적'으로 나온 분석 수
        private double resolutionRate;         // 해결률
        private double resolutionRateDelta;    // 지난 기간 대비 해결률 개선폭
        private long averageDurationSeconds;   // 평균 분석 시간(초)

        private List<SeverityTrend> severityTrend;         // 심각도 추이
        private List<TypeDistribution> typeDistribution;   // 취약점 유형 분포
        private List<RecentAnalysis> recentAnalyses;        // 최근 분석 목록
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeverityTrend {
        private LocalDate date;
        private int critical;
        private int high;
        private int medium;
        private int low;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeDistribution {
        private String type;         // TODO: VulnerabilityType enum과 값 불일치 (아래 설명)
        private String displayName;
        private long count;
        private double ratio;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentAnalysis {
        private Long analysisId;
        private String title;
        private String language;
        private Integer riskScore;   // TODO: AnalysisRequest에 아직 없는 필드 (아래 설명)
        private LocalDateTime createdAt;
    }
}
